package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import objects.Livro; // Importar Livro para estoque aleatório

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class JadeLauncher {

    private static ContainerController mainContainer;
    private static int idVendedorCounter = 1;
    private static int idClienteCounter = 1;
    private static List<String> logRelatorio = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numVendedores = 0;
        int numClientes = 0; // No Notion "agentes" pode se referir a clientes

        while (true) {
            System.out.println("\nMenu da Simulação:");
            System.out.println("1) Definir quantidade de Vendedores");
            System.out.println("2) Definir quantidade de Clientes (Compradores)");
            System.out.println("3) Iniciar Simulação");
            System.out.println("4) Sair");
            System.out.print("Escolha uma opção: ");
            String escolha = scanner.nextLine();

            switch (escolha) {
                case "1":
                    System.out.print("Digite a quantidade de vendedores: ");
                    try {
                        numVendedores = Integer.parseInt(scanner.nextLine());
                        if (numVendedores < 0) numVendedores = 0;
                        System.out.println(numVendedores + " vendedores definidos.");
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Por favor, digite um número.");
                    }
                    break;
                case "2":
                    System.out.print("Digite a quantidade de clientes (compradores): ");
                    try {
                        numClientes = Integer.parseInt(scanner.nextLine());
                        if (numClientes < 0) numClientes = 0;
                        System.out.println(numClientes + " clientes definidos.");
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Por favor, digite um número.");
                    }
                    break;
                case "3":
                    if (numVendedores == 0 && numClientes == 0) {
                        System.out.println("Defina a quantidade de vendedores e/ou clientes antes de iniciar.");
                    } else {
                        iniciarPlataformaJadeEAgentes(numVendedores, numClientes);
                        // Após o JADE iniciar e os agentes executarem (o que é assíncrono),
                        // não temos um "fim de turno" fácil aqui sem um agente controlador.
                        // O "relatório" será o log do console gerado pelos agentes.
                        // Para um relatório mais estruturado, precisaríamos de um mecanismo
                        // para os agentes enviarem dados de volta ou um agente coletor.
                        System.out.println("\n--- Simulação Iniciada ---");
                        System.out.println("Acompanhe o log do console para ver as interações dos agentes.");
                        System.out.println("A interface RMA do JADE também foi iniciada para visualização.");
                        System.out.println("Pressione Enter para voltar ao menu principal após observar a simulação...");
                        scanner.nextLine(); // Pausa para o usuário observar
                        // Para "próximo turno" ou "N turnos", precisaríamos parar e reiniciar agentes
                        // ou ter um agente controlador. Esta versão simples roda uma vez.
                        // Se quiséssemos simular N "cenários", poderíamos chamar
                        // iniciarPlataformaJadeEAgentes em um loop aqui,
                        // mas cada chamada criaria uma nova instância JADE (ou tentaria).
                        // Para uma simulação contínua com turnos, o controle deve ser DENTRO do JADE.
                    }
                    break;
                case "4":
                    System.out.println("Saindo da simulação...");
                    if (mainContainer != null) {
                        try {
                            // Tentar desligar o container principal de forma graciosa
                            // Isso pode não matar todos os agentes imediatamente se eles estiverem bloqueados
                            // ou em loops longos sem checagem de interrupção.
                            mainContainer.kill(); // Tenta matar o container e seus agentes
                        } catch (StaleProxyException e) {
                            // O container já pode ter sido morto ou desconectado
                            System.err.println("Erro ao tentar desligar o container JADE: " + e.getMessage());
                        }
                    }
                    Runtime.instance().shutDown(); // Tenta desligar o runtime do JADE
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void iniciarPlataformaJadeEAgentes(int numVendedores, int numClientes) {
        // Reinicializa contadores de ID para cada nova simulação
        idVendedorCounter = 1; 
        idClienteCounter = 1;
        logRelatorio.clear();

        // Garante que o runtime JADE seja obtido (ou criado se for a primeira vez)
        Runtime rt = Runtime.instance(); 

        // Se já existe um container principal, tentamos matá-lo antes de criar um novo
        // Isso é problemático se quisermos "turnos" DENTRO da mesma instância JADE.
        // Para "iniciar simulação" do zero, é ok.
        if (mainContainer != null) {
            try {
                mainContainer.kill(); // Tenta matar o container anterior e seus agentes
                Thread.sleep(1000); // Pequena pausa para o JADE processar o kill
            } catch (StaleProxyException | InterruptedException e) {
                 System.err.println("Aviso: Problema ao tentar parar container anterior: " + e.getMessage());
            }
        }


        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false"); // Essa gui não é precisa, faremos tudo no console.

        mainContainer = rt.createMainContainer(p);

        if (mainContainer == null) {
            System.err.println("Falha ao criar container principal. Verifique se outra instância JADE não está rodando na mesma porta.");
            return;
        }

        try {
            Random random = new Random();
            Livro[] todosLivros = Livro.values();

            // Criar Vendedores
            for (int i = 0; i < numVendedores; i++) {
                // Criar estoque aleatório para cada vendedor
                StringBuilder estoqueArgs = new StringBuilder();
                int numTiposLivrosParaVendedor = 1 + random.nextInt(Math.min(3, todosLivros.length)); // Vende de 1 a 3 tipos
                
                List<Livro> livrosDisponiveisParaEsteVendedor = new ArrayList<>(Arrays.asList(todosLivros));
                Collections.shuffle(livrosDisponiveisParaEsteVendedor);

                for (int j = 0; j < numTiposLivrosParaVendedor && j < livrosDisponiveisParaEsteVendedor.size() ; j++) {
                    Livro livro = livrosDisponiveisParaEsteVendedor.get(j);
                    double preco = 20.0 + random.nextInt(31); // Preço entre 20-50
                    int quantidade = 1 + random.nextInt(5); // 1-5 unidades
                    if (estoqueArgs.length() > 0) {
                        estoqueArgs.append(",");
                    }
                    estoqueArgs.append(livro.name()).append(":").append(String.format("%.1f", preco).replace(",", ".")).append(":").append(quantidade);
                }
                Object[] vendedorArgs = {estoqueArgs.toString()};
                AgentController vendedor = mainContainer.createNewAgent("vendedor" + (idVendedorCounter++), "agents.VendedorAgent", vendedorArgs);
                vendedor.start();
            }

            // Pausa para vendedores registrarem no DF
            if (numVendedores > 0 && numClientes > 0) {
                 System.out.println("Aguardando vendedores registrarem no DF...");
                 Thread.sleep(2000 + (numVendedores * 100)); // Aumentar um pouco o tempo se houver muitos vendedores
            }

            // Criar Clientes
            for (int i = 0; i < numClientes; i++) {
                if (todosLivros.length == 0) {
                    System.out.println("Nenhum livro definido no enum Livro. Não é possível criar clientes.");
                    break;
                }
                Livro livroDesejado = todosLivros[random.nextInt(todosLivros.length)];
                double precoMaxCliente = 15.0 + random.nextInt(46); // Preço máx entre 15-60
                String[] estrategias = {"A", "B", "C"};
                String estrategiaCliente = estrategias[random.nextInt(estrategias.length)];

                Object[] clienteArgs = {livroDesejado.name() + ":" + String.format("%.1f", precoMaxCliente).replace(",",".") + ":" + estrategiaCliente};
                AgentController cliente = mainContainer.createNewAgent("cliente" + (idClienteCounter++), "agents.ClienteAgent", clienteArgs);
                cliente.start();
            }

        } catch (StaleProxyException e) {
            System.err.println("Erro StaleProxyException ao criar agentes: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrompida: " + e.getMessage());
            e.printStackTrace();
        }
    }
}