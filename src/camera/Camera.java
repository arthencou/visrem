package camera;

import camera.modelos.VIMICRO_VC05688M;
import camera.modelos._Camera;
import camera.util.GBC;
import camera.util.OrderedList;
import camera.util.WrapLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class Camera extends JFrame{

    private static final String NEWLINE = System.getProperty("line.separator");

    // Lista de cameras ativas
    private LinkedList camFramesList;
    private Dimension screenResolution;
    private final JPanel generalPanel = new JPanel();
    //private int totalFrames = 0;

    private JScrollPane myScrollPane;
    private Container myContentPane;

    private Boolean defaultLoginForAll = false;

    private String defRecordingPath = System.getProperty("user.dir") + "/records/";

    public Camera(){
        super("Visualização Remota");
        camFramesList = new LinkedList();
        screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
        createWindow();
    }

/*************************** Criação da Janela ********************************/

    /*
     * Gera a barra de menus e a insere na janela principal do programa.
     */
    private void createMenu(){
        MenuBar myMenuBar = new MenuBar();

        // Menu Arquivo
        Menu menuFile = new Menu(" Arquivo ");
        myMenuBar.add(menuFile);

        // Arquivo > Novo Frame
        {
            MenuItem menuNewFrame = new MenuItem("Novo frame");
            menuNewFrame.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    addFrame();
                }
            });
            menuFile.add(menuNewFrame);
        }

        // Arquivo > Cadastrar Nova Camera
        {
            MenuItem menuRegNewCam = new MenuItem("Cadastrar nova camera");
            menuRegNewCam.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    regNewCam();
                }
            });
            menuFile.add(menuRegNewCam);
        }

        // Arquivo > Editar Camera
        {
            MenuItem menuEditCam = new MenuItem("Editar camera");
            menuEditCam.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editCamera();
                }
            });
            menuFile.add(menuEditCam);
        }

        // Arquivo > Ativar todas as Cameras
        {
            MenuItem menuSetAllCams = new MenuItem("Ativar todas as Cameras");
            menuSetAllCams.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(camFramesList != null)
                    while(camFramesList.size() > 0){
                        ((_Camera) camFramesList.get(0)).remFrame();
                    }
                    LinkedList cams = regCamsFileParser();
                    if(cams != null)
                    for(int i = 0; i < cams.size(); i++){
                        String[] c = (String[]) cams.get(i);
                        boolean dp = c[4].equals("usedefaultpass");
                        boolean sr = c[6].equals("startrec");
                        addFrame(c[0], c[1], c[2], c[3], dp, sr);
                    }
                }
            });
            menuFile.add(menuSetAllCams);
        }
        
        // Arquivo > Desativar todas as Cameras
        {
            MenuItem menuUnsetAllCams = new MenuItem("Desativar todas as Cameras");
            menuUnsetAllCams.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(camFramesList != null)
                    while(camFramesList.size() > 0){
                        ((_Camera) camFramesList.get(0)).remFrame();
                    }
                }
            });
            menuFile.add(menuUnsetAllCams);
        }

        // separador
        menuFile.addSeparator();

        // Arquivo > Sair
        {
            MenuItem menuExit = new MenuItem("Sair");
            menuExit.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });
            menuFile.add(menuExit);
        }


        // Menu Opções
        Menu menuOptions = new Menu(" Opções ");
        myMenuBar.add(menuOptions);

        // Opções > "Forçar dados de Login compartilhados"
        {
            final CheckboxMenuItem menuUseDefaultLogin =
                    new CheckboxMenuItem("Forçar dados de Login compartilhados");
            menuUseDefaultLogin.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(defaultLoginForAll = !defaultLoginForAll)
                        setDefAuth();
                }
            });
            menuOptions.add(menuUseDefaultLogin);
        }

        // Opções > "Caminho padrão para gravação
        {
            final MenuItem menuDefSavPath =
                    new MenuItem("Caminho padrão para gravação");
            menuDefSavPath.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fc.showOpenDialog(Camera.this);
                    File retfile = fc.getSelectedFile();
                    if(retfile == null) return;
                    String path = retfile.getPath();
                    defRecordingPath =
                            (path.equals(null)) ? defRecordingPath : path;
                    System.out.println(defRecordingPath);
                }
            });
            menuOptions.add(menuDefSavPath);
        }

        this.setMenuBar(myMenuBar);
    }
    
    /*
     * Providencia todos os componentes que façam parte da janela principal
     * e os adiciona a ela.
     */
    private void createWindow(){

        this.setSize(720, 550);
        this.setLocation(( screenResolution.width - getSize().width )/2,
                         ( screenResolution.height - getSize().height )/2 );
        this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        this.createMenu();

        // WrapLayout é responsavel pela exibição dinamica dos Frames das
        // cameras conforme o tamanho da janela principal.
        generalPanel.setLayout(new WrapLayout() );

        myScrollPane = new JScrollPane();
        myScrollPane.add( generalPanel );
        myScrollPane.setViewportView( generalPanel );
        myScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        myContentPane = getContentPane();
        myContentPane.add( myScrollPane );

        this.addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                close();
                super.windowClosed(e);
            }
        });

        setVisible( true );
        autoLoad();
    }

/************************** Visualização em Abas ******************************/

    private ControlledTabbedPane myTabbedPane = new ControlledTabbedPane();

    /*
     * Classe herdeira de JTabbedPane, representando um painel de abas
     * que mostram a visualização ampliada das cameras. Essa classe é util 
     * na manipulação organizada das abas.
     */
    class ControlledTabbedPane
            extends JTabbedPane
    {
        private int totalComponents;
        public ControlledTabbedPane(){
            super();
            totalComponents = 0;
        }
        public int getTotalComponents(){
            return totalComponents;
        }
        @Override
        public void addTab(String title, Component component){
            super.addTab(title, component);
            totalComponents++;
        }
        public void remTab(int index){
            this.removeTabAt(index);
            totalComponents--;
            updateTabsList(index - 1);
        }
    }

    // Lista representando o conjunto de Frames ampliados.
    LinkedList tabsList = new LinkedList();

    /*
     * Acrescenta um novo Frame ampliado.
     */
    public void updateTabsList( _Camera newTab ){
        tabsList.addLast( newTab );
    }

    /*
     * Elimina um Frame ampliado estabelecido.
     */
    private void updateTabsList( int index ){
        if(index > -1){
            tabsList.remove(index);
            int size = tabsList.size();
            for(int i = index; i < size; i++){
                _Camera c = (_Camera) tabsList.get(i);
                c.setTabIndex( i+1 );
            }
        }
    }

    /*
     * Cria nova aba que mostra um Frame ampliado.
     */
    public int addTab(String name, Component panel){
        if(panel != null){
            if( myTabbedPane.getTotalComponents() == 0 ){
                myContentPane.remove(0);
                myTabbedPane.addTab("Main", myScrollPane);
            }
            myTabbedPane.addTab(name, new JScrollPane( panel ) );
            myContentPane.add(myTabbedPane);
            this.setVisible(true);
            return myTabbedPane.getTotalComponents() - 1;
        }
        return -1;
    }
    
    /*
     * Remove uma determinada aba
     */
    public void remTab(int index){
        if(index > -1){
            myTabbedPane.remTab(index);
            if(myTabbedPane.getTotalComponents() == 1) {
                myTabbedPane.remTab(0);
                myContentPane.remove(0);
                myContentPane.add( myScrollPane );
            }
            this.setVisible(true);
        }
    }



/******************* Organização dos Frames das Cameras ***********************/

    // Lista representando as cameras que estão ativas como um conjunto de parametros.
    LinkedList addedCamsList = new LinkedList();

    /*
     * Cria uma interface para entrada de dados, solicita uma camera registrada
     * e providencia um novo Frame de acordo com a opção selecionada.
     */
    private void addFrame()
    {
        // Lista de cameras registradas por ordem alfabética
        OrderedList registeredCams = regCamsFileParser();
        // Lista de cameras ativas por ordem alfabética
        OrderedList orderedAddedCamsList = new OrderedList();
        
        // Preenchendo a lista de cameras ativas por ordem alfabética
        for(int i = 0; i < addedCamsList.size(); i++){
            String[] tmp = (String[]) addedCamsList.get(i);
            orderedAddedCamsList.add( tmp );
        }
        
        // Variável "b" é um nome de camera na Lista das cameras ativas.
        String b = null;
        if(orderedAddedCamsList.size() != 0)
            // b recebe o primeiro nome da lista.
            b = ((String[]) orderedAddedCamsList.get(0))[0];

        if(registeredCams != null){
            // Eliminando as cameras ativas da Lista de cameras registradas
            for(int i = 0; i < registeredCams.size(); i++){
                String a = ((String[]) registeredCams.get(i))[0];
                if(a.equals(b)){
                    registeredCams.remove(i);
                    orderedAddedCamsList.remove(0);
                    if(orderedAddedCamsList.size() == 0) break;
                    b = ((String[]) orderedAddedCamsList.get(0))[0];;
                    i--;
                }
            }

            if(registeredCams.size()  == 0){
                JOptionPane.showMessageDialog(this,
                        "Todas as cameras cadastradas já estão ativas",
                        "Atenção!",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String userInput = (String) regListInput(registeredCams);

        //Criar o novo frame de camera com base no input
        if(userInput != null){
            String name, path, port, model;
            name = path = port = model = "";
            boolean useDefaultPass = false;
            boolean startRecording = false;
            String[] tmp = null;
            for(int i = 0; i < registeredCams.size(); i++){
                tmp = (String[]) registeredCams.get(i);
                if(userInput.equals( tmp[0] ) ){
                    name = tmp[0];
                    path = tmp[1];
                    port = tmp[2];
                    model = tmp[3];
                    if( !(useDefaultPass = defaultLoginForAll) )
                        useDefaultPass = "usedefaultpass".equals( tmp[4] );
                    startRecording = "autorec".equals(tmp[6]);
                }
            }
            addFrame(name, path, port, model, useDefaultPass, startRecording);
        }
    }

    /*
     * Providencia um novo Frame de camera com base nos parametros.
     */
    private void addFrame(String name, String path, String port, String model,
            boolean usedefpass, boolean startrec)
    {
        //Testando modelos de Cameras
        _Camera cam = null;
        if( model.equalsIgnoreCase("VIMICRO_VC05688M"))
            cam = new VIMICRO_VC05688M(
                    name, path+":"+port, this, usedefpass, startrec );
        else{
            JOptionPane.showMessageDialog(this,
                    "Modelo não suportado", "Erro!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Criando efetivamente o novo Frame
        JPanel cameraFrame = cam.createFrame();
        camFramesList.addLast(cam);
        String[] tmp = {name, (usedefpass)?"usedefpass":""};
        addedCamsList.addLast(tmp);
        cam.setIndex( /*totalFrames++*/camFramesList.size()-1 );
        generalPanel.add( cameraFrame );
        this.setVisible(true);
    }

    /*
     * Elimina um determinado Frame
     */
    public void remFrame(int index){
        _Camera c = (_Camera) camFramesList.remove(index);
        //c.remFrame();
        addedCamsList.remove(index);
        //totalFrames--;
        for(int i = index; i</*totalFrames*/camFramesList.size(); i++){
            c = (_Camera) camFramesList.get(i);
            c.setIndex(i);
        }
        System.gc();
    }

    /*
     * Termina o programa adequadamente
     */
    private void close(){
        //for(int i = 0; i < totalFrames; i++){
        if(camFramesList!=null){
            while(camFramesList.size() != 0){
                _Camera temp = (_Camera) camFramesList.get(0);
                //temp.stop();
                temp.remFrame();
            }
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Camera.class.getName())
                        .log(Level.SEVERE, null, ex);
            }*/
        }
        System.exit(0);
    }

/************************ Obter Login para Camera *****************************/

    // Dados compartilhados de Login para controle e configuração das cameras.
    private String defaultUser = null;
    private String defaultPass = null;

    /*
     * Providencia dados de Login e os retorna.
     */
    public String[] getAuth(String message, String user, String pass){
        String[] auth = null;
        if(defaultLoginForAll) auth = getDefAuth();
        else{
            auth = new AuthDialog().getAuth(message, user, pass);
        }
        return auth;
    }

    /*
     * Retorna os dados compartilhados de Login e, se necessário, providencia-os.
     */
    public String[] getDefAuth(){
        String auth[] = null;
        if(defaultUser == null){
            auth = new AuthDialog()
                    .getAuth("os campos padrão", defaultUser, defaultPass);
            if(auth != null){
                defaultUser = auth[0];
                defaultPass = auth[1];
            }
        }
        else {
            auth = new String[2];
            auth[0] = defaultUser;
            auth[1] = defaultPass;
        }

        return auth;
    }

    /*
     * Providencia os dados compartilhados de Login e os atualiza em cada
     * camera que os utilize.
     */
    public void setDefAuth(){
        String[] auth = getAuth("os campos padrão", defaultUser, defaultPass);
        if(auth != null){
            if(!auth[0].equals(defaultUser) || !auth[1].equals(defaultPass) ){
                defaultUser = auth[0];
                defaultPass = auth[1];
                // Atualizando os dados de login em cada camera.
                for(int i = 0; i < camFramesList.size(); i++ ){
                    String[] s = (String[]) addedCamsList.get(i);
                    if(s[1].equals("usedefpass") || defaultLoginForAll){
                        _Camera cam = (_Camera) camFramesList.get(i);
                        if(cam.usesDefaultPassword() )
                        cam.setCamsAuth(defaultUser, defaultPass);
                    }
                }
            }
        }
    }

    /*
     * Classe que representa uma interface para entrada de dados de Login
     */
    class AuthDialog extends JDialog {

        private String[] auth = null;
        private JTextField user = new JTextField(10);
        private JPasswordField pass = new JPasswordField(10);
        private JButton okButton = new JButton("OK");
        private JButton cancelButton = new JButton("Cancel");

        public AuthDialog(){
            super(Camera.this, true);
        }

        /*
         * Retorna os dados obtidos do usuario.
         */
        public String[] getAuth(String message, String us, String ps){

            user.setText( (us==null)?"":us );
            pass.setText( (ps==null)?"":ps );
            
            okButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    auth = new String[2];
                    auth[0] = user.getText();
                    auth[1] = new String( pass.getPassword() );
                    setVisible(false);
                }
            });
            
            cancelButton.addActionListener( new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });

            JPanel login = new JPanel( new GridBagLayout() );
            login.add( new JLabel("Usuário: "), new GBC(0,0));
            login.add( user, new GBC(1,0));
            login.add( new JLabel("Senha: "), new GBC(0,1));
            login.add( pass, new GBC(1,1));

            JPanel buttons = new JPanel();
            buttons.setLayout(new GridBagLayout() );
            buttons.add( okButton,
                    new GBC(0,0)
                    .setFill(GBC.HORIZONTAL)
                    .setInsets(0, 5, 0, 0));
            buttons.add( cancelButton,
                    new GBC(0,1)
                    .setFill(GBC.HORIZONTAL)
                    .setInsets(0, 5, 0, 0));
            
            Object klis = new KeyListener(){

                public void keyTyped(KeyEvent e) {}

                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    switch(code){
                        case KeyEvent.VK_ENTER:
                            okButton.doClick();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            cancelButton.doClick();
                            break;
                    }
                }

                public void keyReleased(KeyEvent e) {}
            };

            user.addKeyListener( (KeyListener) klis);
            pass.addKeyListener( (KeyListener) klis);

            this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
            this.setSize( 300, 110 );
            this.setLocation(
                    ( Camera.this.getSize().width - 300 )/2 + Camera.this.getX(),
                    ( Camera.this.getSize().height - 110 )/2 + Camera.this.getY() );
            this.setLayout(new GridBagLayout() );
            this.add(new JLabel("Login para "+message),
                    new GBC(0,0)
                    .setSpan(2, 1));
            this.add(login, new GBC(0,1) );
            this.add(buttons, new GBC(1,1) );
            this.setVisible(true);

            return auth;
        }
    }
    
/************************* Registro das Cameras *******************************/

    /*
     * Providencia uma interface para cadastro de camera.
     */
    private void regNewCam() {
        new RegCamDialog();
    }

    /*
     * Procede de forma organizada para que uma camera possa ser editada.
     */
    private void editCamera() {

        // Lista de cameras registradas
        OrderedList registeredCams = regCamsFileParser();
        // Nome da camera escolhida
        Object userInput = regListInput(registeredCams);

        // Verificando se a camera escolhida encontra-se ativa
        for(int i = 0; i < camFramesList.size(); i++){
            _Camera c = (_Camera) camFramesList.get(i);
            if(c.getCamsName().equals(userInput)){
                int op = JOptionPane.showConfirmDialog(this,
                        "A camera selecionada encontra-se ativa. "
                        + "É necessário finaliza-la para prosseguir."
                        + NEWLINE +"Encerrar atividades da camera?",
                        "Editar " + c.getCamsName(),
                        JOptionPane.YES_NO_OPTION);
                switch(op){
                    case JOptionPane.YES_OPTION:
                        c.remFrame();
                        break;
                    case JOptionPane.NO_OPTION:
                        editCamera();
                        return;
                }
                break;
            }
        }

        // Editando a camera.
        if(userInput != null)
            for(int i = 0; i < registeredCams.size(); i++){
                String[] cam = (String[]) registeredCams.get(i);
                if( userInput != null && userInput.equals( cam[0] ))
                {
                    boolean dpass = cam[4].equals("usedefaultpass");
                    boolean aload = cam[5].equals("autoload");
                    boolean arec = cam[6].equals("autorec");
                    registeredCams.remove(i);
                    new RegCamDialog(registeredCams, cam[0], cam[1],
                            cam[2], dpass, aload, arec);
                }
            }
    }

    /*
     * Classe representando a interface usada para manipular registros
     * de cameras.
     */
    class RegCamDialog extends JDialog {
        
        JTextField nameField;
        JTextField pathField;
        JTextField portField;
        String[] models = { "VIMICRO_VC05688M" };
        JComboBox modelBox = new JComboBox(models);
        JCheckBox checkDefPass;
        JCheckBox checkAutoLoad;
        JCheckBox checkLoadRec;

        OrderedList registeredCams;

        /*
         * Construtor que inicializa os campos de atributos.
         */
        public RegCamDialog(
                OrderedList clist, String name, String path,
                String port, boolean dpass, boolean aload, boolean arec )
        {
            super(Camera.this, true);
            nameField = new JTextField(name, 20);
            pathField = new JTextField(path, 20);
            portField = new JTextField(port, 5);
            checkDefPass = new JCheckBox("Usar dados de login compartilhados", dpass);
            checkAutoLoad =
                    new JCheckBox("Carregar camera automaticamente", aload);
            checkLoadRec = new JCheckBox("Gravar vídeo automaticamente em diretório padrão", arec);
            registeredCams = ( (registeredCams = clist ) == null ) ?
                new OrderedList() : registeredCams;
            regNewCam();
        }

        /*
         * Construtor para atributos em branco.
         */
        public RegCamDialog() {
            this(regCamsFileParser(), null,
                    null, "8080", true, true, true);
        }

        /*
         * Cria a interface de registro e obtem a entrada do usuario.
         */
        private void regNewCam()
        {
            final JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    String name, path, port, model =
                            (String) modelBox.getSelectedItem();
                    boolean useDefPass = checkDefPass.isSelected();
                    boolean setAutoLoad = checkAutoLoad.isSelected();
                    boolean setLoadRec = checkLoadRec.isSelected();
                    if( !(name = nameField.getText()).equals(""))
                    if( !(path = pathField.getText()).equals(""))
                    if( !(port = portField.getText()).equals("")){
                        String pspar = (useDefPass) ? "usedefaultpass" : "";
                        String alpar = (setAutoLoad) ? "autoload" : "";
                        String lrpar = (setLoadRec) ? "autorec" : "";
                        String[] newCam =
                        {name, path, port, model, pspar, alpar, lrpar};
                        registeredCams.add( newCam );
                        putToRegCamsFile(registeredCams);
                        RegCamDialog.this.setVisible(false);
                        int op = JOptionPane.showConfirmDialog(Camera.this,
                                "Deseja visualizar essa camera?",
                                "Registro efetuado",
                                JOptionPane.YES_NO_OPTION);
                        if(op == JOptionPane.YES_OPTION)
                            addFrame(name,path, port, model,
                                    useDefPass, setLoadRec);
                        return;
                    }
                    JOptionPane.showMessageDialog(RegCamDialog.this,
                            "Há Campos não preenchidos",
                            "Erro!",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            final JButton cancelButton = new JButton("Cancelar");
            cancelButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    RegCamDialog.this.setVisible(false);
                }
            });
            
            Object klis = new KeyListener(){

                public void keyTyped(KeyEvent e) {}

                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    switch(code){
                        case KeyEvent.VK_ENTER:
                            okButton.doClick();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            cancelButton.doClick();
                            break;
                    }
                }

                public void keyReleased(KeyEvent e) {}
            };

            okButton.addKeyListener( (KeyListener) klis);
            cancelButton.addKeyListener( (KeyListener) klis);
            nameField.addKeyListener( (KeyListener) klis);
            pathField.addKeyListener( (KeyListener) klis);
            portField.addKeyListener( (KeyListener) klis);
            modelBox.addKeyListener( (KeyListener) klis);
            checkDefPass.addKeyListener( (KeyListener) klis);
            checkAutoLoad.addKeyListener( (KeyListener) klis);
            checkLoadRec.addKeyListener( (KeyListener) klis);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout() );
            panel.add(new JLabel("Título: "),
                    new GBC(0,0)
                    .setAnchor(GBC.LINE_END) );
            panel.add(nameField,
                    new GBC(1,0)
                    .setAnchor(GBC.LINE_START) );
            panel.add(new JLabel("Endereço: "),
                    new GBC(0,1)
                    .setAnchor(GBC.LINE_END) );
            panel.add(pathField,
                    new GBC(1,1)
                    .setAnchor(GBC.LINE_START) );
            panel.add(new JLabel("Porta: "),
                    new GBC(0,2)
                    .setAnchor(GBC.LINE_END) );
            panel.add(portField,
                    new GBC(1,2)
                    .setAnchor(GBC.LINE_START) );
            panel.add(new JLabel("Modelo: "),
                    new GBC(0,3)
                    .setAnchor(GBC.LINE_END) );
            panel.add(modelBox,
                    new GBC(1,3)
                    .setAnchor(GBC.LINE_START) );
            panel.add(checkDefPass,
                    new GBC(1,4)
                    .setAnchor(GBC.LINE_START) );
            panel.add(checkAutoLoad,
                    new GBC(1, 5)
                    .setAnchor(GBC.LINE_START) );
            panel.add(checkLoadRec,
                    new GBC(1, 6));
            panel.add(okButton, new GBC(0,7) );
            panel.add(cancelButton, new GBC(1,7) );

            this.setSize(/*410*/500, 300);
            this.add(panel);
            this.setVisible(true);
        }
    }

    /*
     * Registra um conjunto de cameras (passadas como conjuntos de parametros)
     */
    private void putToRegCamsFile(OrderedList registeredCams) {
        try
        {
            FileWriter out = new FileWriter("regcams.txt");
            for (int i = 0; i < registeredCams.size(); i++) {
                String[] c = (String[]) registeredCams.get(i);
                out.write(NEWLINE + "{ [" + c[0] + "] [" + c[1] + "] ["
                        + c[2] + "] [" + c[3] + "] [" + c[4] + "] ["
                        + c[5] + "] [" + c[6] +"] }" + NEWLINE);
            }
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Retorna o nome de uma camera escolhida com base em uma lista de cameras
     * (representadas como um conjunto de parametros)
     */
    private String regListInput(OrderedList registeredCams){

        //Ler o arquivo de cameras cadastradas
        if(registeredCams == null){
            JOptionPane.showMessageDialog(this,
                    "Nenhum cadastro", "Erro!",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        String[] possibilities = new String[registeredCams.size()];
        for(int i = 0; i < registeredCams.size(); i++){
            possibilities[i] = ( (String[]) registeredCams.get(i) ) [0];
        }

        //Mostrar caixa de diálogo e receber o input
        return new ListInputDialog(true, registeredCams)
                .userInput(possibilities);
    }

    /*
     * Interface para a escolha de cameras em uma caixa de seleção.
     */
    class ListInputDialog extends JDialog {
        
        // Lista de cameras possíveis.
        private OrderedList registeredCams;
        
        public ListInputDialog(boolean modal, OrderedList cams) {
            super(Camera.this, modal);
            registeredCams = cams;
        }

        // Camera escolhida.
        private String userInput = null;
        // Caixa de seleção.
        private JComboBox cams;

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton deleteButton = new JButton("Excluir");

        /*
         * Retorna o nome escolhido na caixa de seleção.
         */
        public String userInput(String[] possibilities){

            cams = new JComboBox(possibilities);

            okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    userInput = (String) cams.getSelectedItem();
                    ListInputDialog.this.setVisible(false);
                }
            });
            cancelButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    ListInputDialog.this.setVisible(false);
                }
            });
            deleteButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if( JOptionPane.showConfirmDialog(ListInputDialog.this,
                            "Deseja mesmo excluir?",
                            "Excluir Camera Cadastrada",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                        return;
                    String input = (String) cams.getSelectedItem();
                    for(int i = 0; i < camFramesList.size(); i++){
                        _Camera c = (_Camera) camFramesList.get(i);
                        if(c.getCamsName().equals(input)) c.remFrame();
                    }
                    for(int i = 0; i < registeredCams.size(); i++){
                        String cam = ( (String[]) registeredCams.get(i) )[0];
                        if(input.equals(cam) ){
                            cams.removeItemAt(i);
                            registeredCams.remove(i);
                            putToRegCamsFile(registeredCams);
                            if(registeredCams.size() == 0){
                                ListInputDialog.this.setVisible(false);
                                userInput = regListInput(null);
                            }
                        }
                    }
                }
            });
            
            Object klis = new KeyListener(){

                public void keyTyped(KeyEvent e) {}

                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    switch(code){
                        case KeyEvent.VK_ENTER:
                            okButton.doClick();
                            break;
                        case KeyEvent.VK_DELETE:
                            deleteButton.doClick();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            cancelButton.doClick();
                            break;
                    }
                }

                public void keyReleased(KeyEvent e) {}
            };

            okButton.addKeyListener( (KeyListener) klis );
            cancelButton.addKeyListener( (KeyListener) klis );
            deleteButton.addKeyListener( (KeyListener) klis );
            cams.addKeyListener( (KeyListener) klis );

            this.addKeyListener( (KeyListener) klis );

            getRootPane().setDefaultButton(okButton);

            JPanel panel = new JPanel( new GridBagLayout() );
            panel.add( new JLabel("Selecione a Camera:"), new GBC(0,0)
                    .setInsets(5) );
            panel.add( cams, new GBC(0,1)
                    .setInsets(5).setSpan(3, 1).setFill(GBC.HORIZONTAL) );
            panel.add( deleteButton, new GBC(3, 1).setInsets(5) );
            panel.add( okButton, new GBC(1,2).setInsets(5) );
            panel.add( cancelButton, new GBC(2,2).setInsets(5) );

            ListInputDialog.this.add(panel);
            ListInputDialog.this.pack();
            ListInputDialog.this.setLocation(
                    (Camera.this.getSize().width - ListInputDialog.this.getSize().width )/2
                    + Camera.this.getX(),
                    (Camera.this.getSize().height - ListInputDialog.this.getSize().height)/2
                    + Camera.this.getY() );
            ListInputDialog.this.setVisible(true);

            return userInput;
        }
    }

    /*
     * Carrega todas as cameras que foram registradas com o paramentro
     * de carregamento automatico.
     */
    private void autoLoad() {
        LinkedList registeredCams = regCamsFileParser();
        String autoload = "autoload";
        if(registeredCams != null)
        for(int i = 0; i < registeredCams.size(); i++){
            String[] cam = (String[]) registeredCams.get(i);
            if( autoload.equals( cam[5] ) ){
                boolean useDefaultPass = (defaultLoginForAll) ? true :
                    "usedefaultpass".equals( cam[4] );
                boolean startRecording = "autorec".equals(cam[6]);
                addFrame(cam[0], cam[1], cam[2], cam[3], useDefaultPass, startRecording);
            }
        }
    }

    /*
     * Retorna uma lista ordenada contendo todas as cameras em regcams.txt
     */
    private OrderedList regCamsFileParser(){
        
        // Arquivo que contem as cameras.
        File regFile = new File("regcams.txt");

        // Lista de cameras encontradas.
        OrderedList allCams = null;
        
        if(regFile.exists() )
        try {
            BufferedReader in =
                    new BufferedReader(new FileReader(regFile));
            
            String line;

            // Número máximo de parametros que representam uma camera
            int totalParameters = 7;

            // A cada linha do arquivo...
            while( (line = in.readLine()) != null){
                boolean waitingNewCamera = true;
                boolean waitingNewParameter = true;
                String name, path, port, model, defpass, autoload, autorec;
                name = path = port = model = defpass = autoload = autorec = "";
                // A partir do primeiro parametro...
                int parCount = 0;
                // Enquanto a linha não acabar...
                for( int i = 0; i < line.length(); i++){
                    char ch = line.charAt(i);
                    if(waitingNewCamera){
                        // Se estiver aguardando nova camera, passe a desconsiderar os "{",
                        // demarcadores de nova camera.
                        if(ch == '{') waitingNewCamera = false;
                    }
                    else if(waitingNewParameter) {
                        // Se estiver aguardando novo parametro ...
                        if(ch == '}'){
                            // se não há mais parametros, aguarde nova camera
                            waitingNewCamera = true;
                            if(parCount == totalParameters){
                                //... e se atingiu o máximo de parametros
                                // adicione essa camera à lista.
                                if(allCams == null)
                                    allCams = new OrderedList();
                                String[] cam = {name, path, port,
                                    model, defpass, autoload, autorec};
                                allCams.add(cam);
                                parCount = 0;
                            }
                        }
                        else if(ch == '['){
                            // ou, se ainda faltam parametros, aguarde-os.
                            waitingNewParameter = false;
                            parCount++;
                        }
                    }
                    else{
                        // Se encontrou um parametro correto, processe-o.
                        if(ch == ']') waitingNewParameter = true;
                        else switch(parCount)
                        {
                            case 1:
                                name = name +String.valueOf(ch);
                                break;
                            case 2:
                                path = path +String.valueOf(ch);
                                break;
                            case 3:
                                port = port +String.valueOf(ch);
                                break;
                            case 4:
                                model = model +String.valueOf(ch);
                                break;
                            case 5:
                                defpass = defpass +String.valueOf(ch);
                                break;
                            case 6:
                                autoload = autoload +String.valueOf(ch);
                                break;
                            case 7:
                                autorec = autorec +String.valueOf(ch);
                                break;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Camera.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return allCams;
    }

/******************************************************************************/

    /*
     * Retorna o diretório padrão para gravação
     */
    public String getDefaultRecPath(){
        return defRecordingPath;
    }

}
