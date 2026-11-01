# hmc-bridge

discord <=> minecraft 1.16.5 chat bridge mod for fabric servers

features:

- discord => minecraft:
  - passes messages through to discord, keeping markdown symbols but resolving mentions and emotes correctly
  - mouse over a display name to see the person's discord username (or APP if webhook)
  - click a message to jump straight to it in discord
  - renders replies
  - cuts off long messages
  - tells you when there is an attachment/embed/poll/sticker/forwarded message that it can't render and links you there
  - `/whitelist` slash command in discord to let people add mc usernames to server whitelist
- minecraft => discord:
  - posts player chat messages as a webhook message with that player's mc username and avatar
  - posts messages when the server starts and stops
  - posts messages when a player joins, leaves, dies, gets an achievement, etc, showing the player's avatar as a small icon alongside

setup:

get a discord bot. give it the privileged content intent. add it to your server. add a webhook to your server. run your minecraft server with the following environment variables set:
- `WEBHOOK_ID`: the first part of the webhook url
- `WEBHOOK_TOKEN`: the second part of the webhook url
- `CHANNEL_ID`: the id of the channel you want the bot to post to
- `BOT_TOKEN`: login token for the bot
