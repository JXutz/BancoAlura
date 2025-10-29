
import OperacoesBanco.Operacoes;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.*;
import java.util.*;

void main() {

    String arquivoPath = Operacoes.escolherArquivoDados();

    if(arquivoPath == null) {
        JOptionPane.showMessageDialog(null, "Arquivo não selecionado. Encerrando...");
        return;
    }

    try {
        Operacoes.carregarContas(arquivoPath);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Erro ao carregar arquivo: " + e.getMessage());
        return;
    }

    boolean continuar = true;
    while (continuar) {
        String menu =
                "Bem-Vindo(a) ao " + Paths.get(arquivoPath).getFileName() + "\n\n" +
                "1 - Consultar saldos\n" +
                "2 - Depositar\n" +
                "3 - Transferir/Sacar\n" +
                "4 - Sair\n\n" +
                "Digite a opção desejada:";

        String opcao = JOptionPane.showInputDialog(menu);

        if (opcao == null) {
            int confirmacao = JOptionPane.showConfirmDialog(null, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) continuar = false;
            continue;
        }

        switch (opcao.trim()) {
            case "1":
                Operacoes.consultarSaldos();
                break;
            case "2":
                Operacoes.depositar(arquivoPath);
                break;
            case "3":
                Operacoes.transferirOuSacar(arquivoPath);
                break;
            case "4":
                int confirmacao = JOptionPane.showConfirmDialog(null, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
                if (confirmacao == JOptionPane.YES_OPTION) continuar = false;
                break;
            default:
                JOptionPane.showMessageDialog(null, "Opção inválida! Tente novamente.");
        }
    }

    JOptionPane.showMessageDialog(null, "Encerrando o sistema... Volte Sempre!");
}



