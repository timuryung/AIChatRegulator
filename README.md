# AI Chat Regulator for Minecraft

AI Chat Regulator is a Bukkit/Spigot plugin designed to enhance the moderation of chat on Minecraft servers. It utilizes a large language model (LLM) to evaluate and filter inappropriate content in real-time, promoting a safer and more welcoming environment for all players.

## Prerequisites

Before using the AI Chat Regulator plugin, ensure that you have **Nitro** installed on your system. Nitro is essential for running the language model required by this plugin.

To install Nitro, run the following command in your terminal:

```
curl -sfL https://raw.githubusercontent.com/janhq/nitro/main/install.sh | sudo /bin/bash -
```

Additionally, you will need to obtain a .gguf model file, which the plugin uses for chat moderation. Place this model file in an accessible location on your server.

After acquiring the .gguf model, specify the path to this model in the plugin's configuration file (config.yml) under the llmModelPath setting. This step is crucial for the plugin to correctly load and utilize the model for processing chat messages.

Ensure that Nitro and the .gguf model are correctly set up and operational before proceeding with the installation of AI Chat Regulator.


## Features

- **Real-time Chat Moderation**: Automatically moderates chat messages using a large language model to detect and filter out inappropriate content.
- **Customizable Warnings**: Supports customizable warning messages that are sent to players who violate chat policies.
- **Inappropriate Message Tracking**: Keeps track of the number of inappropriate messages sent by each player.
- **Easy Configuration**: Offers a straightforward configuration file to customize the path to the LLM model, system prompts, warning messages, and more.

## Installation

1. Download the AI Chat Regulator plugin `.jar` file.
2. Place the downloaded `.jar` file into your server's `plugins` directory.
3. Restart your server, or if your server is already running, load the plugin dynamically if your server software supports it.
4. Upon startup, the plugin will automatically create a configuration file (`config.yml`) in the `plugins/AIChatRegulator` folder. Edit this file as needed to suit your server's requirements.

## Configuration

After the first run, you can configure the plugin by editing the `config.yml` file located in the `plugins/AIChatRegulator` folder. Key configuration options include:

- `llmModelPath`: The path to the LLM model used for chat moderation.
- `systemPrompt`: A customizable system prompt used to instruct the LLM on how to process chat messages.
- `warning_message_template`: The template for warning messages sent to players who send inappropriate content.
- Additional messages and settings to tailor the plugin's operation to your preferences.

Ensure to reload the plugin or restart your server after making changes to the configuration file.

## Usage

Once installed and configured, the plugin operates automatically. It intercepts chat messages, processes them through the LLM, and takes action based on the content's appropriateness. Players who frequently send inappropriate messages can be warned, and their infractions are tracked for server administrators' review.

## Commands

The plugin supports commands for administrators to manage its operation dynamically:

- `/aichat`: Provides basic command functionality and information about the plugin. (Further commands can be added as per requirements.)

## Support

If you encounter any issues or have suggestions for improvements, please feel free to open an issue on the GitHub repository page.

## License

This project is licensed under [specify license], allowing for widespread use and modification in accordance with the license terms.

---

AI Chat Regulator aims to provide Minecraft server administrators with a powerful tool to maintain a healthy chat environment, leveraging advanced AI capabilities to automate the moderation process.
