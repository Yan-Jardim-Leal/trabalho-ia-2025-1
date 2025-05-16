package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JadeLauncher {
	/*
	 * =========================================
	 * 		ALUNOS:
	 * 
	 * 			YAN JARDIM LEAL
	 * 				GABRIEL PEREIRA NEVES
	 * 					ROBERTO MARQUES DIAS
	 * 
	 * =========================================
	 * 
	 */
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false"); // Vamos manter a GUI desligada, creio que o importante seja os bots funcionando no log

        ContainerController mainContainer = rt.createMainContainer(p);

        if (mainContainer == null) {
            System.err.println("Falha ao criar container principal. Verifique se outra instância JADE não está rodando na mesma porta.");
            return;
        }
        
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        
        try {
            // Vendedores
            // Formato args: "LIVRO1:PRECO1:QTD1,LIVRO2:PRECO2:QTD2,..."
            Object[] vendedor1Args = {"HARRY_POTTER:25.0:5,O_SENHOR_DOS_ANEIS:40.0:3"};
            AgentController vendedor1 = mainContainer.createNewAgent("vendedor1", "agents.VendedorAgent", vendedor1Args);
            vendedor1.start();

            Object[] vendedor2Args = {"HARRY_POTTER:28.0:2,AS_CRONICAS_DO_GELO_E_FOGO:35.0:4,APOLOGIA_DE_SOCRATES:22.0:6"};
            AgentController vendedor2 = mainContainer.createNewAgent("vendedor2", "agents.VendedorAgent", vendedor2Args);
            vendedor2.start();
            
            Object[] vendedor3Args = {"UMA_BREVE_HISTORIA_DO_TEMPO:30.0:3,ASSIM_FALOU_ZARATUSTRA:27.0:2"};
            AgentController vendedor3 = mainContainer.createNewAgent("vendedor3", "agents.VendedorAgent", vendedor3Args);
            vendedor3.start();


            // Clientes
            // Formato args: "NOME_LIVRO_ENUM:PRECO_MAXIMO:ESTRATEGIA(A,B,C)"
            Thread.sleep(2000); // Pequena pausa para os vendedores registrarem no DF

            Object[] cliente1Args = {"HARRY_POTTER:27.0:B"}; // Quer Harry Potter por no max R$27, estrategia B
            AgentController cliente1 = mainContainer.createNewAgent("cliente1", "agents.ClienteAgent", cliente1Args);
            cliente1.start();

            Object[] cliente2Args = {"AS_CRONICAS_DO_GELO_E_FOGO:38.0:C"}; // Quer As Crônicas por no max R$38, estrategia C
            AgentController cliente2 = mainContainer.createNewAgent("cliente2", "agents.ClienteAgent", cliente2Args);
            cliente2.start();
            
            Object[] cliente3Args = {"APOLOGIA_DE_SOCRATES:20.0:A"}; // Quer Apologia por no max R$20, estrategia A (tratar A = B por simplicidade)
            AgentController cliente3 = mainContainer.createNewAgent("cliente3", "agents.ClienteAgent", cliente3Args);
            cliente3.start();


        } catch (StaleProxyException e) {
            System.err.println("Erro StaleProxyException ao criar agentes: ");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrompida: ");
            e.printStackTrace();
        }
    }
}