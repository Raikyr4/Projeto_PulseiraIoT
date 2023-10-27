const int trigPin = 2; // Pino de controle do trigger do sensor ultrassônico
const int echoPin = 4; // Pino de controle do echo do sensor ultrassônico
int motorPin = 3; // Pino de controle do motor
bool objetoDetectado = false;

void setup() {
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(motorPin, OUTPUT);
}

void loop() {
  // Dispara o pulso ultrassônico
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  // Mede a duração do pulso recebido pelo sensor
  long duration = pulseIn(echoPin, HIGH);
  
  // Calcula a distância em centímetros
  int distance = duration * 0.034 / 2;

  if (distance <= 110) {
    objetoDetectado = true;
  } else {
    objetoDetectado = false;
  }

  if (objetoDetectado) {
    digitalWrite(motorPin, HIGH); // Liga o motor se um objeto for detectado
  } else {
    digitalWrite(motorPin, LOW); // Desliga o motor se nenhum objeto for detectado
  }

  delay(100); // Aguarda um curto período de tempo para evitar leituras muito frequentes
}
