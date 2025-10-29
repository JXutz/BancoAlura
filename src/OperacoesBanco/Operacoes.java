package OperacoesBanco;

import ContasBanco.Conta;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class Operacoes {
    private static Map<String, Conta> contas = new LinkedHashMap<>();

    public static String escolherArquivoDados() {
        int escolha = JOptionPane.showConfirmDialog(null, "Deseja escolher um arquivo de dados específico?\n" +
                        "Clique em NÃO para usar/gerar o arquivo padrão na pasta do projeto.", "Arquivo de Dados",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) {
            return null;
        }

        if (escolha ==  JOptionPane.YES_OPTION) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione (ou crie) o arquivo .txt onde os dados serão salvos.");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int res = chooser.showSaveDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();

                if (!f.getName().toLowerCase().endsWith(".txt")) {
                    f = new File(f.getParentFile(), f.getName() + ".txt");
                }
                return f.getAbsolutePath();
            } else {
                return null;
            }
        } else {
            Path p = Paths.get("dados_contas.txt");
            return p.toAbsolutePath().toString();
        }
    }

    public static void carregarContas(String caminho) throws IOException {
        Path p = Paths.get(caminho);
        if (!Files.exists(p)) {
            Files.createFile(p);
            return;
        }

        contas.clear();

        try (BufferedReader reader = Files.newBufferedReader(p)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] partes = linha.split(";", -1);
                if (partes.length < 3) continue;
                String id = partes[0].trim();
                String nome = partes[1].trim();
                String saldoStr = partes[2].trim();
                BigDecimal saldo;

                try {
                    saldo = new BigDecimal(saldoStr).setScale(2, RoundingMode.HALF_EVEN);
                } catch (NumberFormatException ex) {
                    saldo = BigDecimal.ZERO.setScale(2);
                }
                Conta c = new Conta(id, nome, saldo);
                contas.put(id, c);
            }
        }
    }

    public static void salvarContas(String caminho) throws IOException {
        Path p = Paths.get(caminho);
        try (BufferedWriter writer = Files.newBufferedWriter(p, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Conta c : contas.values()) {
                String linha = c.getID() + ";" + c.getNome() + ";" + c.getSaldo().toPlainString();
                writer.write(linha);
                writer.newLine();
            }
        }
    }

    public static void consultarSaldos() {
        if (contas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhuma conta cadastrada.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Contas e saldos: \n\n");
        for (Conta c : contas.values()) {
            sb.append(c.getID()).append(" - ").append(c.getNome()).append(" : R$ ").append(format(c.getSaldo())).append("\n");
        }

        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public static void depositar(String caminho) {
        try {
            String id = JOptionPane.showInputDialog("Digite o ID da conta para depósito:");
            if (id == null) return;
            id = id.trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(null, "ID inválido!");
                return;
            }

            Conta c = contas.get(id);
            if (c == null) {
                int criar = JOptionPane.showConfirmDialog(null, "Conta não encontrada. Deseja criar uma nova conta com esse ID?",
                        "Criar conta", JOptionPane.YES_NO_OPTION);
                if (criar != JOptionPane.YES_OPTION) return;

                String nome = JOptionPane.showInputDialog("Digite o nome do(a) titular:");
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nome inválido. Operação cancelada!");
                    return;
                }

                c = new Conta(id, nome.trim(), BigDecimal.ZERO.setScale(2));
                contas.put(id, c);
            }

            String valorStr = JOptionPane.showInputDialog("Use pontos (.) para dividir as casas.\n" + "Digite o valor do depósito:");
            if (valorStr == null) return;
            BigDecimal valor;

            try {
                valor = new BigDecimal(valorStr.replace(",", ".")).setScale(2, RoundingMode.HALF_EVEN);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(null, "Valor deve ser maior que zero (0).");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Valor inválido!");
                return;
            }

            c.setSaldo(c.getSaldo().add(valor));
            salvarContas(caminho);
            JOptionPane.showMessageDialog(null, "Depósito efetuado!\n" + "Saldo atual: R$" + format(c.getSaldo()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar dados: " + e.getMessage());
        }
    }

    public static void transferirOuSacar(String caminho) {
        String[] options = {"Sacar", "Transferir", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(null, "Escolha a operação:", "Sacar ou Transferir",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (escolha == 0){
            sacar(caminho);
        } else if (escolha == 1) {
            transferir(caminho);
        } else {
            // Cancelar
        }
    }

    private static void sacar(String caminho) {
        try {
            String id = JOptionPane.showInputDialog("Digite o ID da conta para saque:");
            if (id == null) return;
            id = id.trim();
            Conta c = contas.get(id);
            if (c == null) {
                JOptionPane.showMessageDialog(null,"Conta não encontrada.");
                return;
            }

            String valorStr = JOptionPane.showInputDialog("Use PONTO (.) para separar as casas\n" + "Digite o valor do saque:");
            if (valorStr == null) return;
            BigDecimal valor;
            try {
                valor = new BigDecimal(valorStr.replace(",", ".")).setScale(2, RoundingMode.HALF_EVEN);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(null, "Valor deve se maior que zero (0).");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Saldo insuficiente.\n" + "Saldo atual: R$" + format(c.getSaldo()));
                return;
            }

            c.setSaldo(c.getSaldo().subtract(valor));
            salvarContas(caminho);
            JOptionPane.showMessageDialog(null, "Saque efetuado!\n" + "Saldo atual: R$" + format(c.getSaldo()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar dados: " + e.getMessage());
        }
    }

    private static void transferir(String caminho) {
        try {
            String idOrigem = JOptionPane.showInputDialog("Digite o número de sua conta:");
            if (idOrigem == null) return;
            idOrigem = idOrigem.trim();
            Conta origem = contas.get(idOrigem);
            if (origem == null) {
                JOptionPane.showMessageDialog(null, "Sua conta não foi encontrada.");
                return;
            }

            String idDestino = JOptionPane.showInputDialog("Digite o número da conta destino:");
            if (idDestino == null) return;
            idDestino = idDestino.trim();
            Conta destino = contas.get(idDestino);
            if (destino == null) {
                int criar = JOptionPane.showConfirmDialog(null, "Conta destino não encontrada!\n" +
                        "Deseja criar um nova conta com esse ID?", "Criar conta", JOptionPane.YES_NO_OPTION);
                if (criar != JOptionPane.YES_OPTION) return;
                String nome = JOptionPane.showInputDialog("Digite o nome do Titular da conta destino:");
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Nome inválido!\n" + "Operação cancelada.");
                    return;
                }
                destino = new Conta(idDestino, nome.trim(), BigDecimal.ZERO.setScale(2));
                contas.put(idDestino, destino);
            }

            String valorStr = JOptionPane.showInputDialog("Use PONTO (.) para dividir as casas decimais." + "Digite o valor a transferir - R$:");
            if (valorStr == null) return;
            BigDecimal valor;
            try {
                valor = new BigDecimal(valorStr.replace(",", ".")).setScale(2, RoundingMode.HALF_EVEN);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(null, "Valor deve ser maior que zero (0).");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Valor inválido!");
                return;
            }

            if (origem.getSaldo().compareTo(valor) < 0) {
                JOptionPane.showMessageDialog(null, "Saldo insuficiente na conta. \n" +
                                                   "Saldo atual: R$" + format(origem.getSaldo()));
                return;
            }

            origem.setSaldo(origem.getSaldo().subtract(valor));
            destino.setSaldo(destino.getSaldo().add(valor));
            salvarContas(caminho);
            JOptionPane.showMessageDialog(null, "Transferência realizada com sucesso! \nSaldo de sua conta/; R$" + format(origem.getSaldo())
                                        + "\nSaldo da conta destino//: R$" + format(destino.getSaldo()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar dados: " + e.getMessage());
        }
    }

    private static String format(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_EVEN).toPlainString();
    }
}

