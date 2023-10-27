## estou usando o replit como compilador
## preciso definir peças quer iria usar : microcontrolador , microsensores e microvibradores 
## Com tais peças deve-se mudar o codigo para que funcione com as peças na simulação
## eh possivel tentar usar uma interface web para demonstrar a simuilação

import math
import random
import time
import tkinter as tk

class MiniSensor:
    def __init__(self, angle):
        self.angle = angle
        self.detecting = False

class Pulseira:
    def __init__(self):
        self.sensores = [MiniSensor(angle) for angle in range(4)]
        self.connected = False

    def detect_objects(self):
        for sensor in self.sensores:
            if self.connected:
                # Simulando a detecção de objetos (para fins de teste)
                # A detecção ocorre apenas se a pulseira estiver ligada
                sensor.detecting = random.choice([True, False])

    def vibrate(self):
        for sensor in self.sensores:
            if sensor.detecting:
                # Simulando uma distância aleatória
                distance = random.uniform(0.1, 2.0)  # Substitua pela lógica real de medição de distância
                # Apenas vibre se a distância for menor ou igual a 1.10m e não for o próprio usuário
                if 0.1 <= distance <= 1.10:
                    vibration_strength = 1 / distance
                    # Simulando a vibração
                    print(f"Sensor {sensor.angle}: Vibrando com força {vibration_strength}")

class App:
    def __init__(self, root):
        self.root = root
        self.root.title("Pulseira IoT")

        self.pulseira = Pulseira()

        self.connect_button = tk.Button(root, text="Ligar/Desligar Pulseira", command=self.toggle_connection)
        self.connect_button.pack()

        self.root.after(1000, self.update)

    def toggle_connection(self):
        self.pulseira.connected = not self.pulseira.connected
        if self.pulseira.connected:
            self.connect_button.config(text="Desligar Pulseira")
        else:
            self.connect_button.config(text="Ligar Pulseira")

    def update(self):
        if self.pulseira.connected:
            self.pulseira.detect_objects()
            self.pulseira.vibrate()
        self.root.after(1000, self.update)

if __name__ == "__main__":
    root = tk.Tk()
    app = App(root)
    root.mainloop()

