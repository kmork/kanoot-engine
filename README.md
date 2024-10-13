# Kanoot Engine

Kanoot Engine is a quiz game server application built using Kotlin and Ktor. This project provides a robust and scalable server for handling game logic and client interactions.
Players should use the corresponding Kanoot Play web app to be quizzed and be square. 

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Running the Server](#running-the-server)
- [Configuration](#configuration)
- [Logging](#logging)
- [Contributing](#contributing)
- [License](#license)

## Features

- Built with Kotlin and Ktor
- JSON serialization with Kotlinx
- Configurable logging with Logback
- Easy routing configuration

## Getting Started

### Prerequisites

- JDK 11 or higher
- Gradle 6.8.3 or higher

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/kanoot.git
    cd kanoot
    ```

2. Build the project:
    ```sh
    ./gradlew build
    ```

### Running the Server

To start the server, run the following command:
```sh
./gradlew run
```

The server will start on http://0.0.0.0:8080.  

## Configuration

The main configuration for the server is located in app/src/main/kotlin/com/knutmork/kanoot/gameserver/Application.kt. You can modify the server port and host in the embeddedServer function.  

## Logging

Logging is configured using Logback. The configuration file is located at app/src/main/kotlin/resources/logback.xml. You can adjust the logging levels and appenders as needed.  

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.  

## License

This project is licensed under the MIT License. See the LICENSE file for details.