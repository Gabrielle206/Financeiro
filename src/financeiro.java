import java.util.Scanner;
import java.sql.*;

public class financeiro {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/financeiro_schema";
    private static final String USER = "root";
    private static final String PASS = "280500";

    private static int idlogado = -1;

    public static void cadastro(Connection connection, Scanner scanner) {
        System.out.println("\n===== Cadastro de usuario =====");

        System.out.print("E-mail: ");
        String email = scanner.nextLine();

        if(verificar(connection, email)){
            System.out.println("Usuario já cadastrado!");
            return;
        }

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        if(email.isEmpty() || senha.isEmpty()) {
            System.out.println("E-mail e senha não podem estar vazios!");
            return;
        }

        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO usuario (email, senha) VALUES (?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, senha);
            pstmt.executeUpdate();
            System.out.println("Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar o usuário: " + e.getMessage());
        }
    }

    public static int login(Connection connection, Scanner scanner) {
        System.out.println("\n===== Login do usuario =====");

        System.out.print("E-mail: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT idusuario FROM usuario WHERE email = ? AND senha = ?")) {
            pstmt.setString(1, email);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()) {
                idlogado = rs.getInt("idusuario");
                System.out.println("Login realizado com sucesso!");
                return idlogado;
            } else {
                System.out.println("Usuario ou senha incorretos.");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar usuário: " + e.getMessage());
            return -1;
        }
    }

    public static boolean verificar(Connection connection, String email) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT idusuario FROM usuario WHERE email = ?")) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar usuário: " + e.getMessage());
            return false;
        }
    }

    public static void receita(Connection connection, Scanner scanner) {
        if(idlogado == -1) {
            System.out.println("É necessário estar logado!");
            return;
        }

        System.out.println("\n===== Cadastro de receita =====");

        System.out.print("Informe a categoria (Salario, Vendas, Outros): ");
        String categoria = scanner.nextLine();

        System.out.print("Informe o valor: ");
        double valor;
        try {
            valor = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido!");
            return;
        }

        System.out.print("Informe a data (AAAA-MM-DD): ");
        String data = scanner.nextLine();

        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO receita (categoria, valor, data, id_usuario) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, categoria);
            pstmt.setDouble(2, valor);
            pstmt.setString(3, data);
            pstmt.setInt(4, idlogado);
            pstmt.executeUpdate();
            System.out.println("Receita cadastrada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar a receita: " + e.getMessage());
        }
    }

    public static void despesa(Connection connection, Scanner scanner) {
        if(idlogado == -1) {
            System.out.println("É necessário estar logado!");
            return;
        }

        System.out.println("\n===== Cadastro de despesas =====");

        System.out.print("Categoria (Alimentação, E-commerce, Viagens, Outros): ");
        String categoria = scanner.nextLine();

        System.out.print("Valor: ");
        double valor;
        try {
            valor = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido!");
            return;
        }

        System.out.print("Forma de pagamento (Pix, Crédito, Débito, Dinheiro, Outros): ");
        String forma_pagamento = scanner.nextLine();

        System.out.print("Data (AAAA-MM-DD): ");
        String data = scanner.nextLine();

        try(PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO despesa (categoria, valor, forma_pagamento, data, id_usuario) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, categoria);
            pstmt.setDouble(2, valor);
            pstmt.setString(3, forma_pagamento);
            pstmt.setString(4, data);
            pstmt.setInt(5, idlogado);
            pstmt.executeUpdate();
            System.out.println("Despesa cadastrada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar a despesa: " + e.getMessage());
        }
    }

    public static void saldo(Connection connection) {
        if(idlogado == -1) {
            System.out.println("É necessário estar logado!");
            return;
        }

        System.out.println("\n===== Analise do saldo atual =====");

        try (PreparedStatement pstmtReceita = connection.prepareStatement(
                "SELECT SUM(valor) AS total_receita FROM receita WHERE id_usuario = ?");
             PreparedStatement pstmtDespesa = connection.prepareStatement(
                     "SELECT SUM(valor) AS total_despesa FROM despesa WHERE id_usuario = ?")) {

            pstmtReceita.setInt(1, idlogado);
            ResultSet receita = pstmtReceita.executeQuery();
            receita.next();
            double totalreceita = receita.getDouble("total_receita");

            pstmtDespesa.setInt(1, idlogado);
            ResultSet despesa = pstmtDespesa.executeQuery();
            despesa.next();
            double totaldespesa = despesa.getDouble("total_despesa");

            double saldo = totalreceita - totaldespesa;

            System.out.printf("Total de Receitas: R$ %.2f\n", totalreceita);
            System.out.printf("Total de Despesas: R$ %.2f\n", totaldespesa);
            System.out.printf("Saldo: R$ %.2f\n", saldo);
        } catch (SQLException e) {
            System.err.println("Erro ao calcular o saldo: " + e.getMessage());
        }
    }

    public static void h_receita(Connection connection) {
        if(idlogado == -1) {
            System.out.println("É necessário estar logado!");
            return;
        }

        System.out.println("\n===== Histórico de Receitas =====");

        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT categoria, valor, data FROM receita WHERE id_usuario = ? ORDER BY data")) {

            pstmt.setInt(1, idlogado);
            ResultSet r = pstmt.executeQuery();

            boolean hasData = false;
            while(r.next()) {
                hasData = true;
                String categoria = r.getString("categoria");
                double valor = r.getDouble("valor");
                String data = r.getString("data");
                System.out.printf("%s | R$ %.2f | %s\n", categoria, valor, data);
            }

            if (!hasData) {
                System.out.println("Nenhuma receita encontrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao acessar o histórico de receitas: " + e.getMessage());
        }
    }

    public static void h_despesas(Connection connection) {
        if(idlogado == -1) {
            System.out.println("É necessário estar logado!");
            return;
        }

        System.out.println("\n===== Histórico de Despesas =====");

        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT categoria, valor, forma_pagamento, data FROM despesa WHERE id_usuario = ? ORDER BY data")) {

            pstmt.setInt(1, idlogado);
            ResultSet d = pstmt.executeQuery();

            boolean hasData = false;
            while(d.next()) {
                hasData = true;
                String categoria = d.getString("categoria");
                double valor = d.getDouble("valor");
                String forma_pagamento = d.getString("forma_pagamento");
                String data = d.getString("data");
                System.out.printf("%s | R$ %.2f | %s | %s\n", categoria, valor, forma_pagamento, data);
            }

            if (!hasData) {
                System.out.println("Nenhuma despesa encontrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao acessar o histórico de despesas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try(Connection connection = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Conexão com o banco de dados estabelecida.");

            int op;

            do {
                System.out.println("\n===== Sistema Financeiro =====");
                System.out.println("1 - Login");
                System.out.println("2 - Cadastro");
                System.out.println("3 - Receita");
                System.out.println("4 - Despesa");
                System.out.println("5 - Saldo");
                System.out.println("6 - Histórico de receitas");
                System.out.println("7 - Histórico de despesas");
                System.out.println("0 - Sair");
                System.out.print("Selecione: ");

                op = scanner.nextInt();
                scanner.nextLine();

                switch (op) {
                    case 1:
                        login(connection, scanner);
                        break;
                    case 2:
                        cadastro(connection, scanner);
                        break;
                    case 3:
                        receita(connection, scanner);
                        break;
                    case 4:
                        despesa(connection, scanner);
                        break;
                    case 5:
                        saldo(connection);
                        break;
                    case 6:
                        h_receita(connection);
                        break;
                    case 7:
                        h_despesas(connection);
                        break;
                    case 0:
                        System.out.println("Saindo do sistema...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }

            } while (op != 0);

        } catch (SQLException e) {
            System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}