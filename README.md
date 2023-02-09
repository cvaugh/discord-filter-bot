# Discord Filter Bot

A bot for filtering words or phrases from a Discord server, written with [JDA](https://github.com/DV8FromTheWorld/JDA).

## Adding the bot to your server

[Click here to add the bot to your server.](https://discord.com/api/oauth2/authorize?client_id=1073085068035231834&permissions=2147576832&scope=bot)

## Commands
| Command                 | Description                                                                                                                                                 |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/addword <word>`       | Add a string (or a list of strings, separated by commas) to the filter list                                                                                 |
| `/removeword <word>`    | Remove a string (or a list of strings, separated by commas) from the filter list                                                                            |
| `/listwords`            | Show the contents of the filter list.                                                                                                                       |
| `/clearwords`           | Clear the filter list.                                                                                                                                      |
| `/filterstrict <state>` | If true, filters strings even if they are part of a larger string. This may cause [unintended filtering](https://en.wikipedia.org/wiki/Scunthorpe_problem). |
| `/filternotify <state>` | If true, the bot will tell users which word caused their message to be removed.                                                                             |

## Self-hosting the bot

1. Download the [latest release](https://github.com/cvaugh/discord-filter-bot/releases)
2. Run the downloaded JAR: `java -jar discord-filter-bot-x.x.x.jar`
3. [Create a Discord Bot](https://discord.com/developers/docs/intro#bots-and-apps)
4. Open `discordfilterbot/config.json` and replace `YOUR TOKEN HERE` (after `botToken`) with your bot's token
5. Run the JAR and start using your bot
