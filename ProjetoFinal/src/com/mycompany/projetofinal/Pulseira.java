package com.mycompany.projetofinal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Pulseira {
    ArrayList<MiniSensor> sensores = new ArrayList<>();
    ArrayList<PessoaConhecida> pessoasConhecidas = new ArrayList<>();
    PessoaNaoCadastrada pessoaNaoCadastrada = new PessoaNaoCadastrada(100, 300);
    boolean connected = false;
    PessoaConhecida selectedPessoa = null;
    PessoaNaoCadastrada selectedPessoaNaoCadastrada = null;

    Pulseira() {
        sensores.add(new MiniSensor(250, 50));
        sensores.add(new MiniSensor(450, 250));
        sensores.add(new MiniSensor(50, 250));
    }

    void cadastrarPessoaNoArquivo(String nome) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contatos.txt", true))) {
            writer.write(nome);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void excluirPessoaDoArquivo(String nome) {
        ArrayList<String> linhas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("contatos.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(nome)) {
                    linhas.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("contatos.txt"))) {
            for (String linha : linhas) {
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    PessoaConhecida getPessoaConhecidaByName(String nome) {
        for (PessoaConhecida pessoa : pessoasConhecidas) {
            if (pessoa.nome.equals(nome)) {
                return pessoa;
            }
        }
        return null;
    }

    void toggleConnection() {
        connected = !connected;
        System.out.println("Pulseira " + (connected ? "ligada." : "desligada."));
    }

    void detectObjects(double mouseX, double mouseY) {
        if (!connected) {
            return; // Se a pulseira não está conectada, retorna sem fazer nada.
        }

        for (MiniSensor sensor : sensores) {
            double distanceToCenter = Math.sqrt(Math.pow((sensor.x - 250), 2) + Math.pow((sensor.y - 250), 2));
            if (distanceToCenter <= 200) {
                double distance = Math.sqrt(Math.pow((sensor.x - mouseX), 2) + Math.pow((sensor.y - mouseY), 2));
                sensor.detecting = distance < 30;
                if (sensor.detecting) {
                    System.out.println("Sensor " + (sensores.indexOf(sensor) + 1) + " vibrando.");
                }
            } else {
                sensor.detecting = false;
            }
        }
    }

    void detectPessoasConhecidas() {
        if (!connected) {
            return; // Se a pulseira não está conectada, retorna sem fazer nada.
        }

        for (PessoaConhecida pessoa : pessoasConhecidas) {
            for (MiniSensor sensor : sensores) {
                double distance = Math.sqrt(Math.pow((pessoa.x - sensor.x), 2) + Math.pow((pessoa.y - sensor.y), 2));
                pessoa.detecting = distance < 30;
                if (pessoa.detecting) {
                    System.out.println("Pessoa Conhecida: " + pessoa.nome + " chegando!");
                }
            }
        }
    }

    void detectPessoaNaoCadastrada() {
        if (!connected) {
            return; // Se a pulseira não está conectada, retorna sem fazer nada.
        }

        for (MiniSensor sensor : sensores) {
            double distance = Math.sqrt(Math.pow((pessoaNaoCadastrada.x - sensor.x), 2) + Math.pow((pessoaNaoCadastrada.y - sensor.y), 2));
            pessoaNaoCadastrada.detecting = distance < 30;
            if (pessoaNaoCadastrada.detecting) {
                System.out.println("Pessoa Não Cadastrada chegando!");
            }
        }
    }

    void vibrate() {
        for (MiniSensor sensor : sensores) {
            if (sensor.detecting) {
                double distance = Math.random() * 1.9 + 0.1;
                if (0.1 <= distance && distance <= 1.1) {
                    double vibrationStrength = 1 / distance;
                    System.out.println("Sensor " + (sensores.indexOf(sensor) + 1) + ": Vibrando com força " + vibrationStrength);
                }
            }
        }
    }

    void addPessoaConhecida(String nome, double x, double y) {
        PessoaConhecida pessoa = new PessoaConhecida(x, y, nome);
        pessoasConhecidas.add(pessoa);
    }

    void removePessoaConhecida(double x, double y) {
        PessoaConhecida pessoaEncontrada = null;
        for (PessoaConhecida pessoa : pessoasConhecidas) {
            if (pessoa.x == x && pessoa.y == y) {
                pessoaEncontrada = pessoa;
                break;
            }
        }

        if (pessoaEncontrada != null) {
            pessoasConhecidas.remove(pessoaEncontrada);
        }
    }

    void loadPessoasCadastradas() {
        try (BufferedReader reader = new BufferedReader(new FileReader("contatos.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addPessoaConhecida(line, 250, 250);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<String> listarNomesDoArquivo() {
        ArrayList<String> nomes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("contatos.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                nomes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nomes;
    }
}
