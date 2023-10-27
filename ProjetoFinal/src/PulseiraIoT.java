import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

class PulseiraIoT {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Pulseira pulseira = new Pulseira();

            File file = new File("contatos.txt");

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            JFrame frame = new JFrame("Pulseira IoT");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JButton connectButton = new JButton("Ligar/Desligar Pulseira");
            connectButton.addActionListener(e -> pulseira.toggleConnection());

            JButton listarNomesButton = new JButton("Listar Nomes");
            listarNomesButton.addActionListener(e -> {
                ArrayList<String> nomes = pulseira.listarNomesDoArquivo();
                StringBuilder nomesStr = new StringBuilder();
                for (String nome : nomes) {
                    nomesStr.append(nome).append("\n");
                }
                JOptionPane.showMessageDialog(frame, nomesStr.toString(), "Nomes Cadastrados", JOptionPane.INFORMATION_MESSAGE);
            });

            JPanel canvasPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.GRAY);
                    g2d.setStroke(new BasicStroke(10));
                    g2d.drawOval(50, 50, 400, 400);

                    for (MiniSensor sensor : pulseira.sensores) {
                        g2d.setColor(Color.BLACK);
                        g2d.fillOval((int) sensor.x - 5, (int) sensor.y - 5, 10, 10);
                    }

                    g2d.setColor(Color.RED);
                    g2d.fillRect((int) pulseira.pessoaNaoCadastrada.x - 10, (int) pulseira.pessoaNaoCadastrada.y - 10, 20, 20);

                    for (PessoaConhecida pessoa : pulseira.pessoasConhecidas) {
                        g2d.setColor(Color.BLUE);
                        g2d.fillRect((int) pessoa.x - 10, (int) pessoa.y - 10, 20, 20);
                    }
                }
            };
            canvasPanel.setPreferredSize(new Dimension(500, 500));

            JLabel messageLabel = new JLabel();
            messageLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));

            JTextField entry = new JTextField(10);

            JButton cadastrarButton = new JButton("Cadastrar Pessoa");
            cadastrarButton.addActionListener(e -> {
                String nome = entry.getText();
                if (!nome.isEmpty()) {
                    pulseira.addPessoaConhecida(nome, 550, 150);
                    pulseira.cadastrarPessoaNoArquivo(nome);
                    canvasPanel.repaint();
                    entry.setText("");
                }
            });

            JButton excluirButton = new JButton("Excluir Pessoa");
            excluirButton.addActionListener(e -> {
                String nome = entry.getText();
                if (!nome.isEmpty()) {
                    boolean resposta = confirmDialog(frame, "Excluir Pessoa", "Deseja realmente excluir a pessoa " + nome + "?");
                    if (resposta) {
                        pulseira.excluirPessoaDoArquivo(nome);
                        PessoaConhecida pessoa = pulseira.getPessoaConhecidaByName(nome);
                        if (pessoa != null) {
                            pulseira.removePessoaConhecida(pessoa.x, pessoa.y);
                        }
                        canvasPanel.repaint();
                    }
                    entry.setText("");
                }
            });

            canvasPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    pulseira.detectObjects(e.getX(), e.getY());
                    pulseira.detectPessoasConhecidas();
                    pulseira.detectPessoaNaoCadastrada();
                    pulseira.vibrate();
                    canvasPanel.repaint();
                }
            });

            canvasPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pulseira.selectedPessoa = null;
                    pulseira.selectedPessoaNaoCadastrada = null;

                    int x = e.getX();
                    int y = e.getY();

                    for (PessoaConhecida pessoa : pulseira.pessoasConhecidas) {
                        if ((pessoa.x - 10) <= x && x <= (pessoa.x + 10) && (pessoa.y - 10) <= y && y <= (pessoa.y + 10)) {
                            pulseira.selectedPessoa = pessoa;
                            pulseira.removePessoaConhecida(pessoa.x, pessoa.y);
                            break;
                        }
                    }

                    if ((pulseira.pessoaNaoCadastrada.x - 10) <= x && x <= (pulseira.pessoaNaoCadastrada.x + 10)
                            && (pulseira.pessoaNaoCadastrada.y - 10) <= y && y <= (pulseira.pessoaNaoCadastrada.y + 10)) {
                        pulseira.selectedPessoaNaoCadastrada = pulseira.pessoaNaoCadastrada;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (pulseira.selectedPessoa != null) {
                        pulseira.addPessoaConhecida(pulseira.selectedPessoa.nome, e.getX(), e.getY());
                        canvasPanel.repaint();
                    } else if (pulseira.selectedPessoaNaoCadastrada != null) {
                        pulseira.pessoaNaoCadastrada.x = e.getX();
                        pulseira.pessoaNaoCadastrada.y = e.getY();
                        canvasPanel.repaint();
                    }
                }
            });

            JPanel controlPanel = new JPanel();
            controlPanel.add(connectButton);
            controlPanel.add(listarNomesButton);
            controlPanel.add(entry);
            controlPanel.add(cadastrarButton);
            controlPanel.add(excluirButton);

            frame.add(controlPanel, BorderLayout.NORTH);
            frame.add(canvasPanel, BorderLayout.CENTER);
            frame.add(messageLabel, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);

            pulseira.loadPessoasCadastradas();
        });
    }

    private static boolean confirmDialog(JFrame frame, String title, String message) {
        int option = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }
}

class MiniSensor {
    double x;
    double y;
    boolean detecting;

    MiniSensor(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

class PessoaConhecida {
    double x;
    double y;
    String nome;
    boolean detecting;

    PessoaConhecida(double x, double y, String nome) {
        this.x = x;
        this.y = y;
        this.nome = nome;
    }
}

class PessoaNaoCadastrada {
    double x;
    double y;
    boolean detecting;

    PessoaNaoCadastrada(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

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
