package com.example.jarvis

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var conversation: TextView
    private lateinit var commandInput: EditText
    private var tts: TextToSpeech? = null
    private var timer: CountDownTimer? = null
    private val speechRequestCode = 501
    private val channelId = "jarvis_timer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        createNotificationChannel()
        buildInterface()
        requestNeededPermissions()
        say("Olá! Sou o JARVIS. Experimente dizer ou digitar: definir timer para 30 minutos.")
    }

    private fun buildInterface() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF07101E.toInt())
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(0xFF49C6FF.toInt())
            letterSpacing = 0.18f
        }
        outer.addView(title, match(WRAP_CONTENT))

        status = TextView(this).apply {
            text = "ASSISTENTE PESSOAL • VERSÃO 0.1"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(0xFF9BB4CB.toInt())
            setPadding(0, 8, 0, 24)
        }
        outer.addView(status, match(WRAP_CONTENT))

        conversation = TextView(this).apply {
            text = "JARVIS: Sistema inicializado.\n"
            textSize = 16f
            setTextColor(0xFFEAF6FF.toInt())
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFF102238.toInt())
        }
        val scroll = ScrollView(this)
        scroll.addView(conversation)
        outer.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        commandInput = EditText(this).apply {
            hint = "Ex.: definir timer para 30 minutos"
            setHintTextColor(0xFF829BB1.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setSingleLine(true)
        }
        outer.addView(commandInput, match(WRAP_CONTENT))

        val send = Button(this).apply {
            text = "Executar comando"
            setOnClickListener { handleCommand(commandInput.text.toString()) }
        }
        outer.addView(send, match(WRAP_CONTENT))

        val voice = Button(this).apply {
            text = "🎙 Falar com o JARVIS"
            setOnClickListener { startSpeechRecognition() }
        }
        outer.addView(voice, match(WRAP_CONTENT))

        val help = TextView(this).apply {
            text = "Comandos iniciais: “timer 30 minutos”, “cancelar timer”, “que horas são?”.\nA ativação contínua “Hey Jarvis” ainda não está implementada."
            textSize = 12f
            setTextColor(0xFF9BB4CB.toInt())
            setPadding(0, 16, 0, 0)
        }
        outer.addView(help, match(WRAP_CONTENT))
        setContentView(outer)
    }

    private fun handleCommand(raw: String) {
        val command = raw.trim().lowercase(Locale("pt", "BR"))
        if (command.isBlank()) return
        append("Você: $raw")
        when {
            command.contains("cancelar timer") || command.contains("parar timer") -> {
                timer?.cancel()
                timer = null
                status.text = "Temporizador cancelado"
                say("Temporizador cancelado.")
            }
            command.contains("timer") || command.contains("temporizador") -> {
                val minutes = Regex("(\\d+)\\s*(minuto|minutos|min)\\b").find(command)
                    ?.groupValues?.get(1)?.toLongOrNull()
                val seconds = Regex("(\\d+)\\s*(segundo|segundos|seg)\\b").find(command)
                    ?.groupValues?.get(1)?.toLongOrNull()
                val duration = when {
                    minutes != null -> TimeUnit.MINUTES.toMillis(minutes)
                    seconds != null -> TimeUnit.SECONDS.toMillis(seconds)
                    else -> null
                }
                if (duration == null || duration <= 0L) {
                    say("Diga, por exemplo, definir timer para 30 minutos.")
                } else {
                    startTimer(duration)
                }
            }
            command.contains("que horas") || command == "hora" -> {
                val now = java.text.SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(java.util.Date())
                say("Agora são $now.")
            }
            else -> say("Ainda estou aprendendo esse comando. Nesta versão consigo iniciar ou cancelar um timer e informar as horas.")
        }
    }

    private fun startTimer(durationMillis: Long) {
        timer?.cancel()
        val totalSeconds = durationMillis / 1000
        status.text = "Timer ativo: ${totalSeconds / 60} min"
        say("Temporizador iniciado.")
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = millisUntilFinished / 60000
                val secs = (millisUntilFinished % 60000) / 1000
                status.text = "Timer: %02d:%02d".format(mins, secs)
            }
            override fun onFinish() {
                status.text = "Temporizador concluído"
                append("JARVIS: O temporizador terminou.")
                say("Atenção. Seu temporizador terminou.")
                showTimerNotification()
            }
        }.start()
        append("JARVIS: Temporizador iniciado para ${totalSeconds / 60} minuto(s).")
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale um comando para o JARVIS")
        }
        try {
            startActivityForResult(intent, speechRequestCode)
        } catch (_: Exception) {
            Toast.makeText(this, "Reconhecimento de voz indisponível neste aparelho.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("API de resultado de atividade mantida para compatibilidade ampla")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == speechRequestCode && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) handleCommand(spoken)
        }
    }

    private fun say(message: String) {
        append("JARVIS: $message")
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_speech")
    }

    private fun append(line: String) {
        if (::conversation.isInitialized) conversation.append("\n$line\n")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Temporizador JARVIS", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Avisos quando um temporizador do JARVIS termina"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun showTimerNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("JARVIS")
            .setContentText("Seu temporizador terminou.")
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
    }

    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 700)
        }
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) tts?.language = Locale("pt", "BR")
    }

    override fun onDestroy() {
        timer?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun match(height: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, height
    )

    companion object {
        private const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
