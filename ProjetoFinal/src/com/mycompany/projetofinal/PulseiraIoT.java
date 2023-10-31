package com.mycompany.projetofinal;

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



