#implementar algo relacionado a persistência de arquivo 
#acrescentar uma funcionalidade nova, como informa o usuário se algum conhecido está chegando 
#fazer um UML do sistema e o documento 
import math
import random
import time
import tkinter as tk

class MiniSensor:
    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.detecting = False

class Pulseira:
    def __init__(self, app):
        self.sensores = [
            MiniSensor(250, 50),   # Ponto superior
            MiniSensor(450, 250),  # Ponto direito
            #MiniSensor(250, 450),  # Ponto inferior
            MiniSensor(50, 250),   # Ponto esquerdo
        ]
        self.connected = False
        self.app = app

    # def detect_objects(self, mouse_x, mouse_y):
    #     for sensor in self.sensores:
    #         if self.connected:
    #             # Verificar se o cursor está fora da circunferência
    #             # distance_to_center = math.sqrt((sensor.x - 250)**2 + (sensor.y - 250)**2)
    #             # if distance_to_center > 200:
                    
    #                 # Calcular a distância entre o cursor e o sensor
    #                 distance = math.sqrt((sensor.x - mouse_x)**2 + (sensor.y - mouse_y)**2)

    #                 # Se a distância for menor que 30 pixels, ativar a detecção
    #                 sensor.detecting = distance < 30
    def detect_objects(self, mouse_x, mouse_y):
        for sensor in self.sensores:
            if self.connected:
                distance_to_center = math.sqrt((sensor.x - 250)**2 + (sensor.y - 250)**2)
                if distance_to_center <= 200:
                    distance = math.sqrt((sensor.x - mouse_x)**2 + (sensor.y - mouse_y)**2)
                    sensor.detecting = distance < 30
                else:
                    sensor.detecting = False

    def vibrate(self):
        for i, sensor in enumerate(self.sensores):
            if sensor.detecting:
                # Simulando uma distância aleatória
                distance = random.uniform(0.1, 2.0)
                if 0.1 <= distance <= 1.10:
                    vibration_strength = 1 / distance
                    self.app.update_message(f"Sensor {i+1}: Vibrando com força {vibration_strength}")

class App:
    def __init__(self, root):
        self.root = root
        self.root.title("Pulseira IoT")

        self.pulseira = Pulseira(self)

        self.connect_button = tk.Button(root, text="Ligar/Desligar Pulseira", command=self.toggle_connection)
        self.connect_button.pack()

        # Criação do canvas para desenhar os elementos
        self.canvas = tk.Canvas(root, width=500, height=500)
        self.canvas.pack()

        # Desenha a circunferência
        self.canvas.create_oval(50, 50, 450, 450, outline="gray", width=10)

        # Desenha os sensores como pontos pretos
        self.sensores = []
        for sensor in self.pulseira.sensores:
            x, y = sensor.x, sensor.y
            self.sensores.append(self.canvas.create_oval(x-5, y-5, x+5, y+5, fill="black"))

        # Adiciona a label para exibir as mensagens
        self.message_label = tk.Label(root, text="", font=("Helvetica", 12))
        self.message_label.pack()

        self.root.after(100, self.update)  # Reduzi o tempo de atualização

        # Vincula o evento de movimento do mouse à função
        self.canvas.bind("<Motion>", self.on_mouse_move)

    def toggle_connection(self):
        self.pulseira.connected = not self.pulseira.connected
        if self.pulseira.connected:
            self.connect_button.config(text="Desligar Pulseira")
        else:
            self.connect_button.config(text="Ligar Pulseira")

    def update(self):
        if self.pulseira.connected:
            self.pulseira.vibrate()
        self.root.after(100, self.update)

    def on_mouse_move(self, event):
        self.pulseira.detect_objects(event.x, event.y)

    def update_message(self, message):
        self.message_label.config(text=message)

if __name__ == "__main__":
    root = tk.Tk()
    app = App(root)
    root.mainloop()
