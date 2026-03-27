# Project Feature Matrix

This matrix provides a high-level overview of the features and areas of concern for each module within the `grimoire` project.

| Module                  | ECS Implementation                                       | Game Engine / Rendering                                       | Networking                                             | Shared Data / DTOs                                     | Application Logic                                           |
| ----------------------- | -------------------------------------------------------- | ------------------------------------------------------------- | ------------------------------------------------------ | ------------------------------------------------------ | ----------------------------------------------------------- |
| **ecs-simulation**      | A complete, standalone ECS with its own `GameLoop`.      | N/A                                                           | N/A                                                    | Defines its own components (`Position`, `Velocity`).   | Contains simulation logic (AI, movement).                   |
| **grimoire-server**     | A server-specific ECS (`EcsWorld`, `EntityManager`).     | N/A                                                           | Contains the main `GameServer` (Netty-based).          | Defines server-side components.                        | Authoritative game logic, persistence, player management.   |
| **grimoire-client-v2**  | A client-side representation of the ECS world.           | Contains a custom OpenGL rendering engine.                    | Client-side networking logic.                          | Consumes DTOs from `grimoire-shared`.                  | Handles user input and renders the game state.              |
| **grimoire-client**     | A client-side representation of the ECS world.           | JavaFX-based UI.                                              | Client-side networking logic.                          | Consumes DTOs from `grimoire-shared`.                  | Older JavaFX client.                                        |
| **grimoire-shared**     | N/A                                                      | N/A                                                           | Defines the network protocol, packets, and codecs.     | Contains shared DTOs for network communication.        | N/A                                                         |
| **net-bullet**          | N/A                                                      | N/A                                                           | A minimal, reusable networking library.                | N/A                                                    | N/A                                                         |
| **october-engine**      | A comprehensive ECS (`World`, `SystemManager`).          | A full-featured game engine with rendering, audio, etc.       | N/A                                                    | Defines its own components.                            | Provides generic engine services.                           |
| **october-application** | N/A                                                      | Consumes `october-engine` to create a game.                   | N/A                                                    | N/A                                                    | Implements the main application and game state management.  |

---

### Key Observations & Recommendations

*   **Redundant ECS:** There are three distinct ECS implementations. We should consolidate them into a single `ecs-core` module.
*   **Duplicate Game Engines:** Both `grimoire-client-v2` and `october-engine` have their own rendering and engine logic. These should be merged into a single `game-engine` module.
*   **Fragmented Networking:** Networking logic is spread across `grimoire-server`, `grimoire-client`, `grimoire-shared`, and `net-bullet`. This should be consolidated into a unified networking module.
*   **Scattered Data Models:** Game state components and DTOs are defined in multiple places. A `game-shared` module would solve this.
