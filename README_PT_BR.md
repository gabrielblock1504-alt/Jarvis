# JARVIS Android — projeto inicial editável

Esta é uma base de código-fonte Android em Kotlin para você estudar e modificar. **Não é um APK instalado nem compilado.**

## O que está incluído
- Interface inicial com nome JARVIS.
- Campo para digitar comandos.
- Botão para reconhecimento de voz do Android (quando o aparelho tiver serviço de reconhecimento disponível).
- Comando de temporizador: exemplos `timer 30 minutos`, `definir timer para 5 minutos`.
- Contagem usando o temporizador do Android e notificação/alarme simples ao terminar.
- Respostas faladas com Text-to-Speech, se disponível.

## Limitações da primeira versão
- Ainda não tem a palavra de ativação contínua “Hey Jarvis”.
- Não inclui uma IA de conversa local avançada.
- Reconhecimento de voz pode depender do serviço instalado e de conexão.
- O temporizador usa a tela/app e uma notificação simples; o comportamento em segundo plano pode variar por versão do Android e fabricante.
- Este código é um ponto de partida. Pode ser necessário ajustar Gradle/SDK no ambiente de compilação.

## Como compilar
Use um ambiente que compile projetos Android nativos, por exemplo Android Studio num computador ou um ambiente Android de desenvolvimento compatível no celular. Abra a pasta do projeto como projeto Gradle e permita sincronizar os plugins. Se o ambiente pedir versões de SDK, instale a SDK compatível ou ajuste `compileSdk`/`targetSdk`.

Para gerar APK: no Android Studio, use **Build > Build APK(s)**. O APK de teste geralmente aparece em `app/build/outputs/apk/debug/`.

## Próximas etapas recomendadas
1. Testar o temporizador.
2. Melhorar a interface.
3. Confirmar o reconhecimento de fala no aparelho.
4. Adicionar uma implementação de palavra de ativação com serviço em primeiro plano e notificação persistente, respeitando as restrições do Android.
5. Avaliar um modelo de IA local adequado ao hardware.

Não coloque chaves secretas de APIs dentro do APK.
