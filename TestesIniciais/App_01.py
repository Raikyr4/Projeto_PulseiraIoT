import math
import random
import tkinter as tk
from tkinter.simpledialog import askstring
from tkinter import messagebox
import os

class MiniSensor:
    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.detecting = False

class PessoaConhecida:
    def __init__(self, x, y, nome):
        self.x = x
        self.y = y
        self.nome = nome
        self.detecting = False

class PessoaNaoCadastrada:
    def __init__(self, x, y):
        self.x = x
        self.y = y

class Pulseira:
    def __init__(self, app):
        self.sensores = [
            MiniSensor(250, 50),   # Ponto superior
            MiniSensor(450, 250),  # Ponto direito
            MiniSensor(50, 250),   # Ponto esquerdo
        ]
        self.pessoas_conhecidas = []  # Inicialmente vazio
        self.pessoa_nao_cadastrada = PessoaNaoCadastrada(100, 300)
        self.connected = False
        self.app = app

    def detect_objects(self, mouse_x, mouse_y):
        for sensor in self.sensores:
            if self.connected:
                distance_to_center = math.sqrt((sensor.x - 250)**2 + (sensor.y - 250)**2)
                if distance_to_center <= 200:
                    distance = math.sqrt((sensor.x - mouse_x)**2 + (sensor.y - mouse_y)**2)
                    sensor.detecting = distance < 30
                else:
                    sensor.detecting = False
            else:
                sensor.detecting = False

    def detect_pessoas_conhecidas(self):
        for pessoa in self.pessoas_conhecidas:
            for sensor in self.sensores:
                distance = math.sqrt((pessoa.x - sensor.x)**2 + (pessoa.y - sensor.y)**2)
                pessoa.detecting = distance < 30
                if pessoa.detecting:
                    self.app.update_message(f"Pessoa Conhecida: {pessoa.nome} chegando!")

    def detect_pessoa_nao_cadastrada(self):
        for sensor in self.sensores:
            distance = math.sqrt((self.pessoa_nao_cadastrada.x - sensor.x)**2 + (self.pessoa_nao_cadastrada.y - sensor.y)**2)
            self.pessoa_nao_cadastrada.detecting = distance < 30
            if self.pessoa_nao_cadastrada.detecting:
                self.app.update_message(f"Pessoa Não Cadastrada chegando!")

    def vibrate(self):
        for i, sensor in enumerate(self.sensores):
            if sensor.detecting:
                distance = random.uniform(0.1, 2.0)
                if 0.1 <= distance <= 1.10:
                    vibration_strength = 1 / distance
                    self.app.update_message(f"Sensor {i+1}: Vibrando com força {vibration_strength}")

    def add_pessoa_conhecida(self, nome, x, y):
        pessoa = PessoaConhecida(x, y, nome)
        self.pessoas_conhecidas.append(pessoa)
        self.app.create_pessoa_conhecida_square(pessoa)
        self.app.save_pessoas_cadastradas()

    def remove_pessoa_conhecida(self, pessoa):
        self.pessoas_conhecidas.remove(pessoa)
        self.app.remove_pessoa_conhecida_square(pessoa)
        self.app.save_pessoas_cadastradas()

    def load_pessoas_cadastradas(self):
        if os.path.exists("contatos.txt"):
            with open("contatos.txt", "r") as file:
                for line in file:
                    nome, x, y = line.strip().split(",")
                    x, y = int(x), int(y)
                    self.add_pessoa_conhecida(nome, x, y)

    def create_contatos_file_if_not_exists(self):
        if not os.path.exists("contatos.txt"):
            open("contatos.txt", "w").close()

class App:
    def __init__(self, root):
        self.root = root
        self.root.title("Pulseira IoT")

        self.pulseira = Pulseira(self)

        self.pulseira.create_contatos_file_if_not_exists()  # Verifica e cria o arquivo contatos.txt se não existir

        self.connect_button = tk.Button(root, text="Ligar/Desligar Pulseira", command=self.toggle_connection)
        self.connect_button.pack()

        self.canvas = tk.Canvas(root, width=500, height=500)
        self.canvas.pack()

        self.canvas.create_oval(50, 50, 450, 450, outline="gray", width=10)

        self.sensores = []
        for sensor in self.pulseira.sensores:
            x, y = sensor.x, sensor.y
            self.sensores.append(self.canvas.create_oval(x-5, y-5, x+5, y+5, fill="black"))

        self.pessoas_conhecidas_squares = []

        self.pessoa_nao_cadastrada = self.canvas.create_rectangle(self.pulseira.pessoa_nao_cadastrada.x-10, self.pulseira.pessoa_nao_cadastrada.y-10, self.pulseira.pessoa_nao_cadastrada.x+10, self.pulseira.pessoa_nao_cadastrada.y+10, fill="red")

        self.message_label = tk.Label(root, text="", font=("Helvetica", 12))
        self.message_label.pack()

        self.entry = tk.Entry(root)
        self.entry.pack()

        self.cadastrar_button = tk.Button(root, text="Cadastrar Pessoa", command=self.cadastrar)
        self.cadastrar_button.pack()

        self.excluir_button = tk.Button(root, text="Excluir Pessoa", command=self.excluir)
        self.excluir_button.pack()

        self.root.after(100, self.update)

        self.canvas.bind("<Motion>", self.on_mouse_move)
        self.canvas.bind("<ButtonPress-1>", self.on_click)
        self.canvas.bind("<B1-Motion>", self.on_drag)

        # Carregar pessoas cadastradas
        self.pulseira.load_pessoas_cadastradas()

    def toggle_connection(self):
        self.pulseira.connected = not self.pulseira.connected
        if self.pulseira.connected:
            self.connect_button.config(text="Desligar Pulseira")
        else:
            self.connect_button.config(text="Ligar Pulseira")

    def update(self):
        if self.pulseira.connected:
            self.pulseira.detect_pessoas_conhecidas()
            self.pulseira.detect_pessoa_nao_cadastrada()
            self.pulseira.vibrate()
        self.root.after(100, self.update)

    def on_mouse_move(self, event):
        self.pulseira.detect_objects(event.x, event.y)
        self.pulseira.vibrate()

    def on_click(self, event):
        for pessoa in self.pulseira.pessoas_conhecidas:
            if (pessoa.x - 10) <= event.x <= (pessoa.x + 10) and (pessoa.y - 10) <= event.y <= (pessoa.y + 10):
                self.selected_pessoa = pessoa
                break
        else:
            self.selected_pessoa = None

        if (self.pulseira.pessoa_nao_cadastrada.x - 10) <= event.x <= (self.pulseira.pessoa_nao_cadastrada.x + 10) and (self.pulseira.pessoa_nao_cadastrada.y - 10) <= event.y <= (self.pulseira.pessoa_nao_cadastrada.y + 10):
            self.selected_pessoa_nao_cadastrada = self.pulseira.pessoa_nao_cadastrada
        else:
            self.selected_pessoa_nao_cadastrada = None

    def on_drag(self, event):
        if self.selected_pessoa:
            self.canvas.coords(self.pessoas_conhecidas_squares[self.pulseira.pessoas_conhecidas.index(self.selected_pessoa)], event.x-10, event.y-10, event.x+10, event.y+10)
            self.selected_pessoa.x = event.x
            self.selected_pessoa.y = event.y
        elif self.selected_pessoa_nao_cadastrada:
            self.canvas.coords(self.pessoa_nao_cadastrada, event.x-10, event.y-10, event.x+10, event.y+10)
            self.pulseira.pessoa_nao_cadastrada.x = event.x
            self.pulseira.pessoa_nao_cadastrada.y = event.y

    def update_message(self, message):
        self.message_label.config(text=message)

    def create_pessoa_conhecida_square(self, pessoa):
        x, y = pessoa.x, pessoa.y
        square = self.canvas.create_rectangle(x-10, y-10, x+10, y+10, fill="blue")
        self.pessoas_conhecidas_squares.append(square)

    def remove_pessoa_conhecida_square(self, pessoa):
        index = self.pulseira.pessoas_conhecidas.index(pessoa)
        self.canvas.itemconfig(self.pessoas_conhecidas_squares[index], fill="red")

    def cadastrar(self):
        nome = self.entry.get()
        if nome:
            self.pulseira.add_pessoa_conhecida(nomez)  # Adiciona com posição padrão
            self.entry.delete(0, tk.END)  # Limpa o campo de texto após cadastrar

    def excluir(self):
        if self.selected_pessoa:
            resposta = messagebox.askyesno("Excluir Pessoa", "Deseja realmente excluir esta pessoa?")
            if resposta:
                self.pulseira.remove_pessoa_conhecida(self.selected_pessoa)
                self.selected_pessoa = None  # Limpa a seleção

    def save_pessoas_cadastradas(self):
        with open("contatos.txt", "w") as file:
            for pessoa in self.pulseira.pessoas_conhecidas:
                file.write(f"{pessoa.nome},{pessoa.x},{pessoa.y}\n")

    def load_pessoas_cadastradas(self):
        if os.path.exists("contatos.txt"):
            with open("contatos.txt", "r") as file:
                for line in file:
                    nome, x, y = line.strip().split(",")
                    x, y = int(x), int(y)
                    self.pulseira.add_pessoa_conhecida(nome, x, y)


if __name__ == "__main__":
    root = tk.Tk()
    app = App(root)
    root.mainloop()
