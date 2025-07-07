package apresentacao;

public class Main {
    public static void main(String[] args) {
        try {

        MenuPrincipal menu = new MenuPrincipal(); 
        menu.iniciar(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}