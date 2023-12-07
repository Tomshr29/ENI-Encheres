package fr.eni.encheres.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ServletConnectDB")
public class ServletConnectDB extends HttpServlet 
{
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        // Définit le type de contenu de la réponse comme HTML
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Récupère les paramètres du formulaire de connexion
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Charge les propriétés depuis le fichier de configuration
        Properties prop = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("/fr/eni/encheres/dal/settings.properties")) 
        {
            prop.load(input);
        } 
        catch (IOException e) 
        {
	        e.printStackTrace();
	        out.println("Erreur de chargement du fichier de configuration.");
	        return;
        }

        // Récupère les informations de connexion depuis les propriétés
        String dbDriver = prop.getProperty("driverdb");
        String dbUrl = prop.getProperty("urldb");
        String dbUser = prop.getProperty("userdb");
        String dbPassword = prop.getProperty("passworddb");

        try 
        {
            // Charge le pilote JDBC spécifié dans le fichier de configuration
            Class.forName(dbDriver);
            
            // Établit la connexion à la base de données avec les informations fournies
            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // Prépare une requête SQL pour vérifier les informations de connexion
            PreparedStatement ps = con.prepareStatement("SELECT * FROM UTILISATEURS WHERE email=? AND mot_de_passe=?");
            ps.setString(1, email);
            ps.setString(2, password);

            // Exécute la requête et récupère le résultat
            ResultSet rs = ps.executeQuery();

            // Vérifie si l'utilisateur existe dans la base de données
            if (rs.next()) 
            {
                // Connexion réussie, stockez la variable de session
                HttpSession session = request.getSession();
                session.setAttribute("userConnected", true);
                session.setAttribute("userEmail", email);

                // Redirection vers index.jsp après connexion réussie
                RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
                rd.forward(request, response);
            }
            else 
            {
                // Affiche un message d'erreur si l'email ou le mot de passe est incorrect
                out.println("Email ou mot de passe incorrect.");
            }

            // Ferme la connexion après utilisation
            con.close();
        } 
        catch (ClassNotFoundException | SQLException e) 
        {
            // Affiche les détails de l'erreur en cas de problème de connexion
            e.printStackTrace();
            out.println("Erreur de connexion à la base de données.");
        }
    }
}