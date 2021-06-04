// Using NodeMCU pins
const int dataPin = D6;   //Outputs the byte to transfer
const int loadPin = D8;   //Controls the internal transference of data in SN74HC595 internal registers
const int clockPin = D7;  //Generates the clock signal to control the transference of data
const int BankA = D3;     // Selects BankA - LEDs 1-8
const int BankB = D4;     // Selects BankB - LEDs 9-16
const int del = 50;
void setup() {
  pinMode(dataPin, OUTPUT);
  pinMode(loadPin, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(BankA, OUTPUT);
  pinMode(BankB, OUTPUT);
  // Set both banks to LOW as default since there is no pull-down on V1 boards
  digitalWrite(BankA, LOW);
  digitalWrite(BankB, LOW);
}

unsigned long integerData = 1;
unsigned long total = 1;

void loop() {
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
  delay(del); 
}
