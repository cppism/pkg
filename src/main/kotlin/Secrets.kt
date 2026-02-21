package pkg

object Secrets {
    val telegramBotToken: String?
        get() = System.getenv("TG_BOT_TOKEN")
}