import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.crypto.*;
import java.awt.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Interface gráfica moderna para o sistema de criptografia.
 * Substitui os JOptionPane por uma janela JFrame com componentes visuais organizados.
 */
public class CriptografiaGUI extends JFrame {
    
    // Componentes da interface
    private JTextArea inputTextArea;
    private JTextArea encryptedOutputArea;
    private JTextArea decryptedOutputArea;
    private JRadioButton aesRadio, desRadio, desedeRadio;
    private ButtonGroup algorithmGroup;
    private JButton encryptButton, decryptButton, clearButton;
    
    // Estado da aplicação
    private SecretKey currentKey;
    private byte[] lastEncrypted;
    
    // Cores do tema
    private static final Color BACKGROUND = new Color(245, 245, 245);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color ACCENT = new Color(63, 81, 181);
    private static final Color TEXT = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    
    public CriptografiaGUI() {
        initializeComponents();
        setupLayout();
        setupListeners();
        updateButtonStates();
    }
    
    private void initializeComponents() {
        // Configuração da janela principal
        setTitle("Sistema de Criptografia - AES, DES, DESede");
        setSize(700, 650);
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        
        // Painel de entrada
        inputTextArea = createStyledTextArea(4, true);
        
        // Painéis de saída
        encryptedOutputArea = createStyledTextArea(3, false);
        decryptedOutputArea = createStyledTextArea(3, false);
        
        // Radio buttons para seleção de algoritmo
        aesRadio = new JRadioButton("AES (Advanced Encryption Standard)", true);
        desRadio = new JRadioButton("DES (Data Encryption Standard)");
        desedeRadio = new JRadioButton("DESede (Triple DES)");
        
        algorithmGroup = new ButtonGroup();
        algorithmGroup.add(aesRadio);
        algorithmGroup.add(desRadio);
        algorithmGroup.add(desedeRadio);
        
        styleRadioButton(aesRadio);
        styleRadioButton(desRadio);
        styleRadioButton(desedeRadio);
        
        // Tooltips
        aesRadio.setToolTipText("Algoritmo de criptografia avançado (128-bit)");
        desRadio.setToolTipText("Algoritmo de criptografia padrão (56-bit)");
        desedeRadio.setToolTipText("Triple DES - maior segurança (168-bit)");
        
        // Botões de ação
        encryptButton = createStyledButton("Criptografar", ACCENT);
        decryptButton = createStyledButton("Descriptografar", new Color(76, 175, 80));
        clearButton = createStyledButton("Limpar", new Color(158, 158, 158));
        
        encryptButton.setToolTipText("Criptografar o texto digitado (Alt+C)");
        decryptButton.setToolTipText("Descriptografar o resultado (Alt+D)");
        clearButton.setToolTipText("Limpar todos os campos (Alt+L)");
        
        // Atalhos de teclado
        encryptButton.setMnemonic('C');
        decryptButton.setMnemonic('D');
        clearButton.setMnemonic('L');
    }

    
    private JTextArea createStyledTextArea(int rows, boolean editable) {
        JTextArea textArea = new JTextArea(rows, 50);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(editable);
        textArea.setBackground(editable ? Color.WHITE : new Color(250, 250, 250));
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return textArea;
    }
    
    private void styleRadioButton(JRadioButton radio) {
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        radio.setBackground(PANEL_BG);
        radio.setForeground(TEXT);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Painel de entrada (NORTH)
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);
        
        // Painel central com controles (CENTER)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        JPanel algorithmPanel = createAlgorithmPanel();
        JPanel actionPanel = createActionPanel();
        
        centerPanel.add(algorithmPanel, BorderLayout.NORTH);
        centerPanel.add(actionPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Painel de resultados (SOUTH)
        JPanel outputPanel = createOutputPanel();
        add(outputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel label = new JLabel("Mensagem para Criptografar:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);
        
        JScrollPane scrollPane = new JScrollPane(inputTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createAlgorithmPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new TitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), 
                "Algoritmo de Criptografia:", 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                TEXT)
        ));
        
        panel.add(aesRadio);
        panel.add(Box.createVerticalStrut(5));
        panel.add(desRadio);
        panel.add(Box.createVerticalStrut(5));
        panel.add(desedeRadio);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panel.setOpaque(false);
        
        panel.add(encryptButton);
        panel.add(decryptButton);
        panel.add(clearButton);
        
        return panel;
    }

    
    private JPanel createOutputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Resultado criptografado
        JLabel encryptedLabel = new JLabel("Resultado Criptografado:");
        encryptedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        encryptedLabel.setForeground(TEXT);
        encryptedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane encryptedScroll = new JScrollPane(encryptedOutputArea);
        encryptedScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        encryptedScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Resultado descriptografado
        JLabel decryptedLabel = new JLabel("Resultado Descriptografado:");
        decryptedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        decryptedLabel.setForeground(TEXT);
        decryptedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane decryptedScroll = new JScrollPane(decryptedOutputArea);
        decryptedScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        decryptedScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(encryptedLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(encryptedScroll);
        panel.add(Box.createVerticalStrut(10));
        panel.add(decryptedLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(decryptedScroll);
        
        return panel;
    }
    
    private void setupListeners() {
        // Listener para habilitar/desabilitar botão criptografar
        inputTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateButtonStates(); }
        });
        
        // Ações dos botões
        encryptButton.addActionListener(e -> handleEncrypt());
        decryptButton.addActionListener(e -> handleDecrypt());
        clearButton.addActionListener(e -> handleClear());
    }
    
    private void handleEncrypt() {
        try {
            String text = inputTextArea.getText();
            String algorithm = getSelectedAlgorithm();
            
            // Gerar chave
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            currentKey = keyGenerator.generateKey();
            
            // Criptografar
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, currentKey);
            lastEncrypted = cipher.doFinal(text.getBytes());
            
            // Exibir resultado em Base64 (mais legível que bytes brutos)
            String encryptedText = Base64.getEncoder().encodeToString(lastEncrypted);
            encryptedOutputArea.setText(encryptedText);
            
            updateButtonStates();
            
        } catch (NoSuchAlgorithmException e) {
            showError("Erro de Algoritmo", "Algoritmo de criptografia não suportado", e);
        } catch (NoSuchPaddingException e) {
            showError("Erro de Padding", "Esquema de padding não disponível", e);
        } catch (InvalidKeyException e) {
            showError("Erro de Chave", "Chave de criptografia inválida", e);
        } catch (IllegalBlockSizeException e) {
            showError("Erro de Tamanho", "Tamanho de dados inválido para o algoritmo", e);
        } catch (BadPaddingException e) {
            showError("Erro de Padding", "Erro ao processar dados", e);
        }
    }
    
    private void handleDecrypt() {
        if (lastEncrypted == null || currentKey == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhuma mensagem criptografada disponível.\nPor favor, criptografe uma mensagem primeiro.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String algorithm = getSelectedAlgorithm();
            
            // Descriptografar
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, currentKey);
            byte[] decrypted = cipher.doFinal(lastEncrypted);
            
            // Exibir resultado
            decryptedOutputArea.setText(new String(decrypted));
            
        } catch (NoSuchAlgorithmException e) {
            showError("Erro de Algoritmo", "Algoritmo de criptografia não suportado", e);
        } catch (NoSuchPaddingException e) {
            showError("Erro de Padding", "Esquema de padding não disponível", e);
        } catch (InvalidKeyException e) {
            showError("Erro de Chave", "Chave de criptografia inválida", e);
        } catch (IllegalBlockSizeException e) {
            showError("Erro de Tamanho", "Tamanho de dados inválido para o algoritmo", e);
        } catch (BadPaddingException e) {
            showError("Erro de Descriptografia", "Dados criptografados corrompidos ou chave incorreta", e);
        }
    }

    
    private void handleClear() {
        inputTextArea.setText("");
        encryptedOutputArea.setText("");
        decryptedOutputArea.setText("");
        aesRadio.setSelected(true);
        currentKey = null;
        lastEncrypted = null;
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        boolean hasInput = !inputTextArea.getText().trim().isEmpty();
        boolean hasEncrypted = lastEncrypted != null;
        
        encryptButton.setEnabled(hasInput);
        decryptButton.setEnabled(hasEncrypted);
    }
    
    private String getSelectedAlgorithm() {
        if (aesRadio.isSelected()) return "AES";
        if (desRadio.isSelected()) return "DES";
        if (desedeRadio.isSelected()) return "DESede";
        return "AES"; // padrão
    }
    
    private void showError(String title, String message, Exception e) {
        String fullMessage = message;
        if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
            fullMessage += "\n\nDetalhes: " + e.getMessage();
        }
        
        JOptionPane.showMessageDialog(
            this,
            fullMessage,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public static void main(String[] args) {
        // Usar look and feel do sistema para melhor integração
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se falhar, usa o padrão
        }
        
        // Criar GUI na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            CriptografiaGUI gui = new CriptografiaGUI();
            gui.setVisible(true);
        });
    }
}
