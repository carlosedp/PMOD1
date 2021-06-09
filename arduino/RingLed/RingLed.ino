#define ENCODER_DO_NOT_USE_INTERRUPTS
#include <Encoder.h>

// Using NodeMCU pins
// RingLED Pins
const int dataPin = D6;   //Outputs the byte to transfer
const int loadPin = D8;   //Controls the internal transference of data in SN74HC595 internal registers
const int clockPin = D7;  //Generates the clock signal to control the transference of data
const int BankA = D3;     // Selects BankA - LEDs 1-8
const int BankB = D4;     // Selects BankB - LEDs 9-16
long interval = 200;

// Encoder Pins
int encoder0PinA = D1;
int encoder0PinB = D2;
int encoder0PinSW = D0;

Encoder myEnc(encoder0PinA, encoder0PinB);
long position  = -999;

void setup() {
  Serial.begin (115200);
  pinMode(dataPin, OUTPUT);
  pinMode(loadPin, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(BankA, OUTPUT);
  pinMode(BankB, OUTPUT);
  // Set both banks to LOW as default since there is no pull-down on V1 boards
  digitalWrite(BankA, LOW);
  digitalWrite(BankB, LOW);
}

void rotEncoder() {
  long newPos = myEnc.read();
  if (newPos != position) {
    if (newPos > position) {
       Serial.print("Clockwise: ");
       interval = interval + 1;
    } else {
      if (interval >= 4) {
        Serial.print("Counter Clockwise: ");
        interval = interval - 1;
      }
    }
    Serial.print(interval);
    Serial.print(" - ");
    Serial.println(position);
    position = newPos;
  }
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

void loop() {
  ringLED();
  rotEncoder();
}
