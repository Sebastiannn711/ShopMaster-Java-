import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


class Producto {
    private String nombre;
    private int stock;
    private double precio;

    
    public Producto(String nombre, int stock, double precio) {
        this.nombre = nombre;
        this.stock = stock;
        this.precio = precio;
    }

    // Getters para que los métodos de MySQL puedan leer los datos
    public String getNombre() { return this.nombre; }
    public int getStock() { return this.stock; }
    public double getPrecio() { return this.precio; }

    // El proyector de pantalla para mostrar los detalles
    public void mostrarInfo() {
        System.out.println("-> Nombre: " + this.nombre + " | Precio: $" + this.precio + " | Stock: " + this.stock);
    }
}

// 2. EL MOTOR PRINCIPAL DE MYSQL
class Main {
    // Las variables de conexión de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/shopmaster";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "1070veloza";

    private static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in); // Prendemos el oído
        int opcion;

        System.out.println("=========================================");
        System.out.println("  SISTEMA SHOPMASTER - MÓDULO PRODUCTOS  ");
        System.out.println("=========================================");

        // El bucle Do-While para mantener el menú vivo
        do {
            System.out.println("\n--- MENÚ DE OPCIONES ---");
            System.out.println("1. Registrar Producto (INSERT)");
            System.out.println("2. Ver Catálogo (SELECT)");
            System.out.println("3. Actualizar Stock (UPDATE)");
            System.out.println("4. Eliminar Producto (DELETE)");
            System.out.println("5. Salir del Sistema");
            System.out.print("Seleccione una opción: ");
            opcion = teclado.nextInt();
            teclado.nextLine(); // El limpiador de canal para que no se salte los textos

    
            switch (opcion) {
                case 1:
                    // Formulario interactivo 
                    System.out.print("Nombre del producto: ");
                    String nom = teclado.nextLine();
                    System.out.print("Cantidad en stock: ");
                    int stk = teclado.nextInt();
                    System.out.print("Precio unitario: ");
                    double pre = teclado.nextDouble();
                    
                    // Creamos el objeto con las respuestas del usuario
                    Producto nuevo = new Producto(nom, stk, pre);
                    registrarProducto(nuevo); // Se lo pasamos al MySQL
                    break;

                case 2:
                    visualizarCatalogo();
                    break;

                case 3:
                    System.out.print("Nombre del producto a actualizar: ");
                    String nomUp = teclado.nextLine();
                    System.out.print("Nuevo stock disponible: ");
                    int nuevoStk = teclado.nextInt();
                    actualizarStock(nomUp, nuevoStk);
                    break;

                case 4:
                    System.out.print("Nombre del producto a eliminar: ");
                    String nomDel = teclado.nextLine();
                    eliminarProducto(nomDel);
                    break;

                case 5:
                    System.out.println("Cerrando el módulo ShopMaster. ¡Hasta pronto!");
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        } while (opcion != 5);

        teclado.close(); // Apagamos el oído al salir
    }

    // =================================================================
    // MÉTODOS CRUD 
    // =================================================================

    // 1: Guardar en la base de datos (INSERT)
    private static void registrarProducto(Producto prod) {
        String sql = "INSERT INTO producto (nombre, stock, precio) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Rellenamos los 3 signos de interrogación con los Getters del objeto
            ps.setString(1, prod.getNombre());
            ps.setInt(2, prod.getStock());
            ps.setDouble(3, prod.getPrecio());
            
            ps.executeUpdate(); // Enviar por el puerto 3306
            System.out.println("¡[ÉXITO] Producto registrado correctamente en ShopMaster!");
            
        } catch (SQLException e) {
            System.out.println("[ERROR] No se pudo registrar: " + e.getMessage());
        }
    }

    // 2: Traer los datos a la pantalla (SELECT)
    private static void visualizarCatalogo() {
        String sql = "SELECT nombre, stock, precio FROM producto";
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS EN BASE DE DATOS ---");
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // El ResultSet es la cuadrícula que recibe los datos
            
            boolean hayDatos = false;
            while (rs.next()) { // Mientras haya filas en la cuadrícula de MySQL
                hayDatos = true;
                // Creamos un objeto temporal por cada fila encontrada
                Producto p = new Producto(rs.getString("nombre"), rs.getInt("stock"), rs.getDouble("precio"));
                p.mostrarInfo(); // Pintamos la info en la terminal
            }
            if (!hayDatos) System.out.println("El catálogo está vacío actualmente.");
            
        } catch (SQLException e) {
            System.out.println("[ERROR] Al leer los datos: " + e.getMessage());
        }
    }

    // 3: Modificar datos existentes (UPDATE)
    private static void actualizarStock(String nombre, int nuevoStock) {
        String sql = "UPDATE producto SET stock = ? WHERE nombre = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, nuevoStock);
            ps.setString(2, nombre);
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("¡[ÉXITO] Stock actualizado correctamente!");
            } else {
                System.out.println("[AVISO] No se encontró ningún producto con ese nombre.");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Al actualizar: " + e.getMessage());
        }
    }

    // 4: Borrar registros (DELETE)
    private static void eliminarProducto(String nombre) {
        String sql = "DELETE FROM producto WHERE nombre = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("¡[ÉXITO] Producto removido del inventario!");
            } else {
                System.out.println("[AVISO] No se encontró el producto.");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Al eliminar: " + e.getMessage());
        }
    }
}
