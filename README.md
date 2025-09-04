# SelfStorage Plugin

A simple, efficient, and persistent GUI-based personal storage/vault plugin for Spigot-based Minecraft servers.

## Features

*   **Personal Vaults:** Provides each player with their own personal storage that can be accessed from anywhere.
*   **GUI Interface:** An intuitive and easy-to-use chest-like GUI for managing stored items.
*   **Data Persistence:** Saves player vaults and their contents, ensuring that items are kept safe between server restarts.
*   **Item Serialization:** Safely stores items with all their custom data (names, lore, enchantments, etc.).
*   **Lightweight:** Designed to be simple and have a minimal impact on server performance.
*   **Easy Configuration:** Comes with a straightforward `config.yml` for basic setup.

## Commands

The main command to interact with the plugin is `/selfstorage`.

*   `/selfstorage open` - Opens your personal storage vault.

## Permissions

*   `selfstorage.use` - Grants access to the `/selfstorage open` command.
*   `selfstorage.admin` - (Optional) For potential future administrative commands.

## Installation

1.  Download the latest version of `selfstorage-1.0-SNAPSHOT.jar` from the releases page.
2.  Place the `.jar` file into the `plugins/` directory of your Minecraft server.
3.  Restart or reload your server. The plugin will create its necessary configuration files.

## Configuration

The main configuration file is located at `plugins/SelfStorage/config.yml`. You can modify basic settings in this file.

```yaml
# Example config.yml
storage:
  title: "My Personal Storage"
  size: 54 # Must be a multiple of 9
```

## Building from Source

If you want to build the plugin yourself, you can do so with Apache Maven.

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/your-username/selfstorage.git
    cd selfstorage
    ```

2.  **Build with Maven:**
    ```sh
    mvn clean package
    ```

The compiled `.jar` file will be located in the `target/` directory.
