// Using ESP32-C3 NodeMCU pins
// RingLED Pins

const int dataPin = 6;   //Outputs the byte to transfer (Ser)
const int loadPin = 7;   //Controls the internal transference of data in SN74HC595 internal registers (RClk)
const int clockPin = 8;  //Generates the clock signal to control the transference of data (SRClk)
const int BankA = 18;     // Selects BankA - LEDs 1-8
const int BankB = 19;     // Selects BankB - LEDs 9-16
long interval = 200;

// Encoder Pins
int encoder0PinA = 1;
int encoder0PinB = 2;
int encoder0PinSW = 10;

int counter = 0;                    //Use this variable to store "steps"
int currentStateClock;              //Store the status of the clock pin (HIGH or LOW)
int lastStateClock;                 //Store the PREVIOUS status of the clock pin (HIGH or LOW)
String currentDir ="";              //Use this to print text 

void setup() {
  Serial.begin (115200);
  Serial.print("Starting up...");
  pinMode(dataPin, OUTPUT);
  pinMode(loadPin, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(BankA, OUTPUT);
  pinMode(BankB, OUTPUT);
  // Set both banks to LOW as default since there is no pull-down resistors on V1 boards
  digitalWrite(BankA, LOW);
  digitalWrite(BankB, LOW);

  pinMode(encoder0PinA,INPUT_PULLUP);
  pinMode(encoder0PinB,INPUT_PULLUP);
  pinMode(encoder0PinSW, INPUT);

  lastStateClock = digitalRead(encoder0PinA);
}


unsigned long integerData = 1;
unsigned long total = 1;
void ringLED() {
  static long currentMillis;
  if (millis() - currentMillis >= interval) {
    if (total <=  128) {digitalWrite(BankA, HIGH);digitalWrite(BankB, LOW);}
    if (total >  128) {digitalWrite(BankA, LOW);digitalWrite(BankB, HIGH);}
    // Send data to IC
    digitalWrite(loadPin, LOW);
    shiftOut(dataPin, clockPin, LSBFIRST, integerData);
    digitalWrite(loadPin, HIGH);
    // Shift bits
    integerData = integerData << 1;
    total = total << 1;
    // Wrap around
    if (integerData > 128) {integerData = 1;}
    if (total > 65535) {total = 1;}
    currentMillis = millis();
   }
}

void rotEncoder() {
  currentStateClock = digitalRead(encoder0PinA);

  // If last and current state of Clock are different, then "pulse occurred"
  // React to only 1 state change to avoid double count
  if (currentStateClock != lastStateClock  && currentStateClock == 1){

    // If the Data state is different than the Clock state then
    // the encoder is rotating "CCW" so we decrement
    if (digitalRead(encoder0PinB) != currentStateClock) {
      counter --;
      currentDir ="Counterclockwise";
      interval --;
    } else {
      // Encoder is rotating CW so increment
      counter ++;
      currentDir ="Clockwise";
      interval ++;
    }

    Serial.print("Direction: ");
    Serial.print(currentDir);
    Serial.print(" | Counter: ");
    Serial.println(counter);
  }

  // We save last Clock state for next loop
  lastStateClock = currentStateClock;
}

void loop() {
  // Reset interval to default 200ms on button push
  if (digitalRead(encoder0PinSW) == HIGH) {
    interval = 200;
    Serial.print("Reset interval to: ");
    Serial.println(interval);
  }
  ringLED();
  rotEncoder();
}
