package org.timurarahost.aichatregulator;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public final class AIChatRegulator extends JavaPlugin {
    private Process nitroProcess;
    private final Queue<String> messageQueue = new LinkedList<>();
    private boolean isProcessing = false;
    private boolean isModelLoaded = false;

    private final Map<String, Integer> inappropriateMessagesCount = new HashMap<>();

    public boolean isModelLoaded() {
        return isModelLoaded;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String modelPath = getConfig().getString("llmModelPath", "/default/path/to/model");
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        startNitroProcess(modelPath);
        this.getCommand("aichat").setExecutor(new PluginCommandExecutor(this));
    }

    private void startNitroProcess(String modelPath) {
        new Thread(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("nitro");
                nitroProcess = processBuilder.start();
                getLogger().info(getConfig().getString("startup_message", "Default"));
                Thread.sleep(20000);
                loadLLMModel(modelPath);
            } catch (Exception e) {
                getLogger().severe(getConfig().getString("llm_startup_error_message", "Default") + e.getMessage());
            }
        }).start();
    }

    public synchronized void enqueueChatMessage(Player player, String message) {
        // Combine player's name with the message or use a Pair<Player, String> to store both
        messageQueue.add(player.getName() + ":" + message);
        if (!isProcessing) {
            processNextMessage();
        }
    }

    private synchronized void processNextMessage() {
        if (messageQueue.isEmpty()) {
            isProcessing = false;
            return;
        }

        isProcessing = true;
        String playerMessagePair = messageQueue.poll(); // Get the next item in the queue
        String[] parts = playerMessagePair.split(":", 2);
        Player player = getServer().getPlayer(parts[0]);
        String message = parts.length > 1 ? parts[1] : "";

        // Check if player is online before processing the message
        if (player != null && player.isOnline()) {
            processChatMessage(player, message);
        } else {
            // Process the next message if the player is offline
            processNextMessage();
        }
    }



    public void processChatMessage(Player player, String message) {
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                URL url = new URL("http://localhost:3928/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonPayload = new JSONObject();
                JSONArray messagesArray = new JSONArray();

                // System instruction
                String systemInstruction = getConfig().getString("systemPrompt", "Default system prompt if none is defined in config.");
                String fullMessage = systemInstruction + message;

                // Add user message with system instruction
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", fullMessage);
                messagesArray.put(userMessage);

                jsonPayload.put("messages", messagesArray);

                // Log the JSON payload before sending
                getLogger().info("Sending JSON payload to LLM API: " + jsonPayload.toString());

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Read and handle the response
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                    String jsonResponse = scanner.useDelimiter("\\A").next();
                    JSONObject response = new JSONObject(jsonResponse);

                    JSONArray choicesArray = response.getJSONArray("choices");
                    if (choicesArray.length() > 0) {
                        JSONObject firstChoice = choicesArray.getJSONObject(0);
                        JSONObject messageObject = firstChoice.getJSONObject("message");
                        String contentResponse = messageObject.getString("content");
                        if (contentResponse.contains("HARMFUL") || contentResponse.contains("ВРЕДНО")) {
                            incrementInappropriateMessageCount(player.getName());
                            // Extract reason from response if available
                            int harmfulIndex = contentResponse.indexOf("HARMFUL");
                            String reason = "";
                            if (contentResponse.contains("HARMFUL")) {
                                reason = contentResponse.substring(contentResponse.indexOf("HARMFUL"));
                            } else if (contentResponse.contains("ВРЕДНО")) {
                                reason = contentResponse.substring(contentResponse.indexOf("ВРЕДНО"));
                            }
                            reason = reason.split("\\.")[0] + ".";
                            // Issue warning to the player
                            if (player != null && player.isOnline()) {
                                player.sendMessage(getConfig().getString("warning_message_template", "Default warning message template if none is defined in config.") + message);
                                player.sendMessage(getConfig().getString("warning_message", "Default warning message if none is defined in config.") + reason);
                            }
                        }
                        getLogger().info(getConfig().getString("llm_response_message", "Default") + contentResponse);
                    } else {
                        getLogger().info(getConfig().getString("no_content_error_message", "Default"));
                    }
                }
            } catch (Exception e) {
                getLogger().severe(getConfig().getString("api_error_message", "Default") + e.getMessage());
            } finally {
                processNextMessage();
            }
        }).start();
    }



    private void loadLLMModel(String modelPath) {

        try {
            URL url = new URL("http://localhost:3928/inferences/llamacpp/loadmodel");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"llama_model_path\": \"" + modelPath + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            Scanner scanner = new Scanner(conn.getInputStream());
            String jsonResponse = scanner.useDelimiter("\\A").next();
            scanner.close();

            // Parse JSON response
            JSONObject response = new JSONObject(jsonResponse);
            String message = response.getString("message");
            if ("Model already loaded".equals(message)) {
                getLogger().info(getConfig().getString("model_already_loaded_message", "Default"));
            } else if ("Failed to load model".equals(message)) {
                getLogger().severe(getConfig().getString("failed_to_load_model_message", "Default"));
            } else {
                getLogger().info(ChatColor.GREEN + message);
                isModelLoaded = true;
            }
        } catch (Exception e) {
            getLogger().severe(getConfig().getString("api_fail_message", "Default") + e.getMessage());
        }
    }

    public void incrementInappropriateMessageCount(String playerName) {
        inappropriateMessagesCount.merge(playerName, 1, Integer::sum);
    }

    public int getInappropriateMessageCount(String playerName) {
        return inappropriateMessagesCount.getOrDefault(playerName, 0);
    }

    @Override
    public void onDisable() {
        // Stop 'nitro' process
        if (nitroProcess != null) {
            nitroProcess.destroy();
            getLogger().info(getConfig().getString("shutdown_message", "Default"));
        }

        // Plugin shutdown logic
    }
}
