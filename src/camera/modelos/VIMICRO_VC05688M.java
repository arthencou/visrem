package camera.modelos;

import camera.util.ExtensionFilter;
import camera.util.GBC;
import camera.util.HTTPAuthenticator;
import java.awt.event.ActionEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.ICodec.Type;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;

/*
 * Classe que representa a camera VIMICRO_VC05688M e modelos compatíveis, além
 * da interface para sua utilização.
 */
public class VIMICRO_VC05688M extends _Camera {
    // Thread de conexão da camera.
    public IP_Cam cameraThread;
    
    // Local da camera.
    private String myCodeBase;
    private String myLocation;
    private String myAuthorization;
    
    // Comandos da camera.
    private String turnRight;
    private String turnLeft;
    private String turnUp;
    private String turnDown;
    private String turnHomePosition;
    private String autoHorizontalScan;
    private String autoVerticalScan;
    private String brightnessPlus;
    private String brightnessLess;
    private String brightnessSTD;
    private String contrastPlus;
    private String contrastLess;
    private String contrastSTD;
    private String set640x480;
    private String set320x240;
    private String set160x120;
    private String qualityHigh;
    private String qualityStandard;
    private String qualityLow;

    // Objetos para a interface.
    private ImageIcon cameraImages1;
    private ImageIcon cameraImages2;
    private JLabel label1;
    private JLabel label2;
    private JPanel expandedFrame;
    
    // Objetos para a gravação.
    private IContainer outContainer = null;
    private IStream outStream;
    private IStreamCoder outStreamCoder;
    private ICodec codec;

    // Informações de ajuste.
    private boolean setRecord = false;
    private String recPath = null;
    
    public VIMICRO_VC05688M( String name, String Base, camera.Camera parentWindow,
            Boolean useDefaultPass, Boolean startRecording )
    {
        super(name, parentWindow, useDefaultPass);

        if(startRecording) recPath = myParentWindow.getDefaultRecPath();

        myCodeBase = Base;
        myLocation = "/cgi-bin/sf.cgi";
        myAuthorization = null;

        turnRight = "/cgi-bin/action?action=cam_mv&diretion=cam_right&lang=eng";
        turnLeft = "/cgi-bin/action?action=cam_mv&diretion=cam_left&lang=eng";
        turnUp = "/cgi-bin/action?action=cam_mv&diretion=cam_up&lang=eng";
        turnDown = "/cgi-bin/action?action=cam_mv&diretion=cam_down&lang=eng";
        turnHomePosition = "/cgi-bin/action?action=cam_mv&diretion=cam_home&lang=eng";
        autoHorizontalScan = "/cgi-bin/action?action=pan_scan&lang=eng";
        autoVerticalScan = "/cgi-bin/action?action=tilt_scan&lang=eng";

        brightnessPlus = "/cgi-bin/action?action=bright_chg&CamInfo_Brightness=add&lang=eng";
        brightnessLess = "/cgi-bin/action?action=bright_chg&CamInfo_Brightness=sub&lang=eng";
        brightnessSTD = "/cgi-bin/action?action=bright_chg&CamInfo_Brightness=std&lang=eng";

        contrastPlus = "/cgi-bin/action?action=contrast_chg&CamInfo_Contrast=add&lang=eng";
        contrastLess = "/cgi-bin/action?action=contrast_chg&CamInfo_Contrast=sub&lang=eng";
        contrastSTD = "/cgi-bin/action?action=contrast_chg&CamInfo_Contrast=std&lang=eng";

        set640x480 = "/cgi-bin/action?action=resolution_chg&CamInfo_Resolution=640x480&lang=eng";
        set320x240 = "/cgi-bin/action?action=resolution_chg&CamInfo_Resolution=320x240&lang=eng";
        set160x120 = "/cgi-bin/action?action=resolution_chg&CamInfo_Resolution=160x120&lang=eng";

        qualityHigh = "/cgi-bin/action?action=compress_chg&CamInfo_Compression=20&lang=eng";
        qualityStandard = "/cgi-bin/action?action=compress_chg&CamInfo_Compression=30&lang=eng";
        qualityLow = "/cgi-bin/action?action=compress_chg&CamInfo_Compression=60&lang=eng";

        cameraImages1 = new ImageIcon();
        cameraImages2 = new ImageIcon();
        label1 = null;
        label2 = null;
        cameraFrame = null;
        expandedFrame = null;
    }

/************************* Criação de Frames *****************************/

    /*
     * Retorna o Painel que representa o Frame da camera.
     */
    @Override
    public JPanel createFrame() {
        try
        {
            cameraFrame = new JPanel();
            cameraFrame.setLayout(new GridBagLayout());

            // Quadro para visualização das imagens
            label1 = new JLabel(cameraImages1);
            cameraFrame.add(label1, new GBC(0, 0));

            // Exibir nome da camera
            JLabel title = new JLabel(myCamsName);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            cameraFrame.add(title, new GBC(0, 1).setInsets(5));
            
            String path = System.getProperty("user.dir");

            /*==================== Barra de ferramentas ======================*/

            JPanel taskBar = new JPanel(new GridBagLayout());
            
            String icon;

            // Botão "Mudar dados de autenticação"
            icon = path +"/images/lock.gif";
            JButton newpass = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            newpass.setToolTipText("Mudar dados de autenticação");
            newpass.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    useDefaultPass = false;
                    setCamsAuth();
                }
            });
            newpass.setPreferredSize( new Dimension(22,22) );
            taskBar.add(newpass);

            // Botão "Usar dados de autenticação compartilhados"
            icon =  path +"/images/users.gif";
            JButton defpass = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            defpass.setToolTipText("Usar dados de autenticação compartilhados");
            defpass.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    useDefaultPass = true;
                    setCamsAuth();
                }
            });
            defpass.setPreferredSize( new Dimension(22,22) );
            taskBar.add(defpass);
            
            // Botão "Snapshot"
            icon =  path +"/images/camera.gif";
            JButton snapshot = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            snapshot.setToolTipText("Snapshot");
            snapshot.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    MyFileChooser fc = new MyFileChooser("jpg");
                    fc.showSaveDialog(myParentWindow);
                    File save = fc.getSelectedFile();
                    try {
                        ImageIO.write( (BufferedImage) cameraImages2.getImage(),
                                "jpg", save);
                    } catch (IOException ex) {
                        Logger.getLogger(VIMICRO_VC05688M.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                }
            });
            snapshot.setPreferredSize( new Dimension(22,22));
            taskBar.add(snapshot);

            // Botão "Re-conectar"
            icon = path +"/images/arrows_circle.gif";
            JButton refresh = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            refresh.setToolTipText("Re-conectar");
            refresh.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    cameraThread.stop();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VIMICRO_VC05688M.
                                class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cameraThread.start();
                    return;
                }
            });
            refresh.setPreferredSize( new Dimension(22,22) );
            taskBar.add(refresh);

            // Botão "Fechar frame"
            icon = path +"/images/application_remove.gif";
            JButton close = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            close.setToolTipText("Fechar frame");
            close.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    remFrame();
                }
            });
            close.setPreferredSize( new Dimension(22,22) );
            taskBar.add(close);

            JPanel controlPanel = new JPanel(new GridBagLayout());

            cameraFrame.add(taskBar, new GBC(0, 2));

            /*===================== Botões direcionais =======================*/
            
            JPanel directionsPanel = new JPanel();
            directionsPanel.setLayout(new GridBagLayout());
            
            // Botão Auto-Horizontal
            icon = path + "/images/ah.png";
            JButton buttonAutoHor = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonAutoHor.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, autoHorizontalScan);
                }
            });
            buttonAutoHor.setPreferredSize( new Dimension(22,22) );
            buttonAutoHor.setBorderPainted(false);
            directionsPanel.add(buttonAutoHor, new GBC(0, 0).setInsets(2, 5, 0, 2));

            // Botão UP
            icon = path + "/images/up.png";
            JButton buttonUp = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonUp.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, turnUp);
                }
            });
            buttonUp.setPreferredSize( new Dimension(22,22) );
            buttonUp.setBorderPainted(false);
            directionsPanel.add(buttonUp, new GBC(1, 1).setInsets(4, 2, 2, 2));

            // Botão Auto-Vertical
            icon = path + "/images/av.png";
            JButton buttonAutoVer = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonAutoVer.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, autoVerticalScan);
                }
            });
            buttonAutoVer.setPreferredSize( new Dimension(22,22) );
            buttonAutoVer.setBorderPainted(false);
            directionsPanel.add(buttonAutoVer, new GBC(2, 0).setInsets(2, 2, 2, 5));

            // Botão Left
            icon = path + "/images/left.png";
            JButton buttonLeft = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonLeft.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, turnLeft);
                }
            });
            buttonLeft.setPreferredSize( new Dimension(22,22) );
            buttonLeft.setBorderPainted(false);
            directionsPanel.add(buttonLeft, new GBC(0, 2));

            // Botão Home-Position
            icon = path + "/images/hp.png";
            JButton buttonHomePos = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonHomePos.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, turnHomePosition);
                }
            });
            buttonHomePos.setPreferredSize( new Dimension(22,22) );
            buttonHomePos.setBorderPainted(false);
            directionsPanel.add(buttonHomePos, new GBC(1, 2).setInsets(2));
            
            // Botão Right
            icon = path + "/images/right.png";
            JButton buttonRight = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonRight.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, turnRight);
                }
            });
            buttonRight.setPreferredSize( new Dimension(22,22) );
            buttonRight.setBorderPainted(false);
            directionsPanel.add(buttonRight, new GBC(2, 2));
            
            // Botão Down
            icon = path + "/images/down.png";
            JButton buttonDown = new JButton(new ImageIcon( ImageIO.read( new File(icon)) ) );
            buttonDown.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, turnDown);
                }
            });
            buttonDown.setPreferredSize( new Dimension(22,22) );
            buttonDown.setBorderPainted(false);
            directionsPanel.add(buttonDown, new GBC(1, 3).setInsets(2, 0, 5, 0));

            controlPanel.add(directionsPanel, new GBC(0,0).setInsets(2));
            
            /*===================== Checks de qualidade ======================*/
            JPanel qualityPanel = new JPanel(new GridBagLayout());

            qualityPanel.add(new JLabel("Resolução:"), new GBC(0,0));

            ButtonGroup group1 = new ButtonGroup();

            // Opção 160x120
            JRadioButton _160 = new JRadioButton("160x120");
            _160.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, set160x120);
                }
            });
            group1.add(_160);
            qualityPanel.add(_160, new GBC(0, 1));

            // Opção 320x240
            JRadioButton _320 = new JRadioButton("320x240");
            _320.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, set320x240);
                }
            });
            group1.add(_320);
            _320.doClick();
            qualityPanel.add(_320, new GBC(0, 2));

            // Opção 640x480
            JRadioButton _640 = new JRadioButton("640x480");
            _640.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, set640x480);
                }
            });
            group1.add(_640);
            qualityPanel.add(_640, new GBC(0,3));

            qualityPanel.add(new JLabel("Qualidade:"), new GBC(2,0));

            ButtonGroup group2 = new ButtonGroup();

            // Opção Alta Qualidade
            JRadioButton _High = new JRadioButton("High");
            _High.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, qualityHigh);
                }
            });
            group2.add(_High);
            qualityPanel.add(_High, new GBC(2, 1).setAnchor(GBC.LINE_START));

            // Opção Qualidade Média
            JRadioButton _Standard = new JRadioButton("Standard");
            _Standard.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, qualityStandard);
                }
            });
            group2.add(_Standard);
            _Standard.doClick();
            qualityPanel.add(_Standard, new GBC(2, 2).setAnchor(GBC.LINE_START));
            
            // Opção Qualidade Baixa
            JRadioButton _Low = new JRadioButton("Low");
            _Low.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, qualityLow);
                }
            });
            group2.add(_Low);
            qualityPanel.add(_Low, new GBC(2, 3).setAnchor(GBC.LINE_START));

            controlPanel.add(qualityPanel, new GBC(1,0).setInsets(2));
            
            /*==================== Botões de configuração ====================*/
            
            JPanel configPanel = new JPanel(new GridBagLayout());
            configPanel.add(new JLabel("Brilho:"), new GBC(0, 0)/*.setSpan(2, 1)*/.setAnchor(GBC.LINE_START));

            
            JPanel bPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEADING, 0, 0));
            // Botão menos brilho
            JButton _bLess = new JButton("-");
            _bLess.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, brightnessLess);
                }
            });
            bPanel.add(_bLess);

            // Botão brilho padrão
            JButton _bSTD = new JButton("STD");
            _bSTD.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, brightnessSTD);
                }
            });
            bPanel.add(_bSTD);
            
            // Botão mais brilho
            JButton _bPlus = new JButton("+");
            _bPlus.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, brightnessPlus);
                }
            });
            bPanel.add(_bPlus);
            configPanel.add(bPanel, new GBC(0,1).setAnchor(GBC.LINE_START) );
            
            configPanel.add(new JLabel("Contraste:"), new GBC(0, 2)/*.setSpan(2, 1)*/.setAnchor(GBC.LINE_START));

            JPanel cPanel = new JPanel(
                    new FlowLayout(FlowLayout.LEADING, 0, 0));
            
            // Botão menos contraste
            JButton _cLess = new JButton("-");
            _cLess.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, contrastLess);
                }
            });
            cPanel.add(_cLess);

            // Botão contraste padrão
            JButton _cSTD = new JButton("STD");
            _cSTD.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, contrastSTD);
                }
            });
            cPanel.add(_cSTD);

            // Botão mais contraste
            JButton _cPlus = new JButton("+");
            _cPlus.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    new DefAction(new HTTPAuthenticator(myUser, myPass), myCodeBase, contrastPlus);
                }
            });
            cPanel.add(_cPlus);
            configPanel.add(cPanel, new GBC(0,3).setAnchor(GBC.LINE_START));

            /*===================== Botões Adicionais ========================*/
            JPanel checks = new JPanel(new GridBagLayout() );

            // Opção Expandir
            JCheckBox expand = new JCheckBox("Expandir");
            expand.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(expandedFrame == null){
                        if ( (tabbedPaneIndex = myParentWindow
                                .addTab( cameraThread.getCamName(),
                                createExpandedFrame() )) != -1 )
                            updateTabsList();
                    }
                    else {
                        myParentWindow.remTab( tabbedPaneIndex );
                        tabbedPaneIndex = -1;
                        expandedFrame = null;
                    }
                    System.gc();
                }
            });
            checks.add(expand, new GBC(0,0));
            
            // Opção Gravar
            final JCheckBox record = new JCheckBox("Gravar");
            record.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(!setRecord){
                        String filepath = null;
                        // Escolhendo caminho de gravação.
                        if(recPath == null){
                            int usedefpath =
                            JOptionPane.showConfirmDialog(myParentWindow,
                                    "Usar caminho padrão?", myCamsName,
                                    JOptionPane.YES_NO_CANCEL_OPTION);
                            switch(usedefpath){
                                case JOptionPane.CANCEL_OPTION:
                                    record.setSelected(false);
                                    return;
                                case JOptionPane.YES_OPTION:
                                    recPath = myParentWindow.getDefaultRecPath();
                                    break;
                                case JOptionPane.NO_OPTION:
                                    recPath = null;
                                    break;
                            }
                        }
                        if(recPath == null){
                            MyFileChooser fc = new MyFileChooser("mov");
                            if( fc.showSaveDialog(myParentWindow) != 0 ){
                                System.out.println(" Caminho Inválido");
                                record.setSelected(false);
                                return;
                            }
                            filepath = fc.getSelectedFile().getAbsolutePath();
                        }
                        else{
                            // Criando arquivo com nome da camera e data
                            // de gravação.
                            Date date = new Date(System.currentTimeMillis());
                            SimpleDateFormat formatedDate =
                                    new SimpleDateFormat("yyyy-MM-dd");
                            int i = 1;
                            String n = "";
                            do{
                                filepath = recPath + myCamsName + " - "
                                    +formatedDate.format(date) + n +".mov";
                                n = " ("+((Object) i++).toString()+")";
                            }while(new File(filepath).exists());
                        }
                        System.out.println(filepath);
                        if( !(setRecord = prepareToRec(filepath)) ){
                            record.setSelected(false);
                            JOptionPane.showMessageDialog(myParentWindow,
                                    "Erro ao Gravar!"+NEWLINE
                                    +"Não pôde completar a operação",
                                    myCamsName,
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else{
                        stopRecording();
                    }
                }
            });
            checks.add(record, new GBC(1,0));
            if(recPath != null){
                setRecord = false;
                record.doClick();
            }

            configPanel.add(checks, new GBC(0,4).setSpan(4, 1));

            controlPanel.add(configPanel, new GBC(2,0).setInsets(2));

            cameraFrame.add(controlPanel, new GBC(0, 3));
            
            cameraThread = new IP_Cam(myCamsName, myCodeBase, myLocation, myAuthorization, this);
            cameraThread.start();
            
            return cameraFrame;
        } catch (IOException ex) {
            Logger.getLogger(VIMICRO_VC05688M.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     * Cria um Frame com a imagem ampliada da camera.
     */
    private JPanel createExpandedFrame() {
        label2 = new JLabel(cameraImages2 );
        expandedFrame = new JPanel(new GridBagLayout() );
        expandedFrame.add(label2, new GBC(0,0) );

        return expandedFrame;
    }
    
    /*
     * Código obtido de http://wiki.xuggle.com/Encoding_Video_from_a_sequence_of_Images
     * acessível em 20/10/2011.
     * A função prepara um arquivo de vídeo que receberá as imagens da camera.
     */
    boolean prepareToRec(String filepath){
        outContainer = IContainer.make();
        if( outContainer.open(filepath, IContainer.Type.WRITE, null) < 0 ){
            outContainer = null;

            return false;
        }

        outStream = outContainer.addNewStream(0);
        outStreamCoder = outStream.getStreamCoder();
        codec = ICodec.guessEncodingCodec(null, null, filepath,
                null, Type.CODEC_TYPE_VIDEO);

        outStreamCoder.setCodec(codec);
        outStreamCoder.setBitRate(25000);
        outStreamCoder.setBitRateTolerance(9000);
        outStreamCoder.setPixelType(IPixelFormat.Type.YUV420P);
        outStreamCoder.setHeight(480);
        outStreamCoder.setWidth(640);
        outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        outStreamCoder.setGlobalQuality(0);

        IRational frameRate = IRational.make(3,1);
        outStreamCoder.setFrameRate(frameRate);
        outStreamCoder.setTimeBase(IRational.make(frameRate
                .getDenominator(), frameRate.getNumerator()));
        frameRate = null;

        outStreamCoder.open();
        outContainer.writeHeader();

        return true;
    }

    /*
     * A função finaliza o arquivo de vídeo da gravação.
     */
    void stopRecording(){
        setRecord = false;
        recPath = null;
        if(outContainer != null){
            outContainer.writeTrailer();
            outContainer = null;
            System.gc();
        }
        System.out.println("Parando gravação de: " +this.myCamsName);
    }

    /*
     * Classe que representa uma thread enviando um comando para a camera.
     */
    class DefAction extends Thread{
        HTTPAuthenticator myHTTPAuthenticator;
        String myCodeBase;
        String myScriptAction;
        public DefAction(HTTPAuthenticator httpAuthenticator,
            String codeBase, String scriptAction){
            super();
            this.myHTTPAuthenticator = httpAuthenticator;
            this.myCodeBase = codeBase;
            this.myScriptAction = scriptAction;
            start();
        }
        @Override
        public void run(){
            try {
                if (VIMICRO_VC05688M.this.needAuthentication)
                    Authenticator.setDefault(myHTTPAuthenticator);
                URL url = new URL(myCodeBase + myScriptAction);
                System.out.println(myCodeBase + myScriptAction);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();
                //connection.setConnectTimeout(3000);
                connection.setRequestProperty("Request-Method", "GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuffer newData = new StringBuffer(1000);
                String s = "";
                while (null != (s = br.readLine())) {
                    newData.append(s);
                }
                br.close();

                System.out.println(new String(newData));

                System.out.println("Response: " + connection.getResponseCode()
                        + "/" + connection.getResponseMessage());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(myParentWindow,
                        "Autenticação Falhou!" +NEWLINE
                        +"Problemas com conexão "
                        +"ou usuário e/ou senha inválido(s)", myCamsName,
                        JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(VIMICRO_VC05688M.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    /*
     * Faz uma chamada adequada ao procedimento de adição de Frame ampliado
     * na janela principal.
     */
    public void updateTabsList(){
        myParentWindow.updateTabsList(this);
    }
    
/************************* Funcionamento do Frame *****************************/

    /*
     * Finaliza todas as atividades da camera e remove o Frame.
     */
    @Override
    public void remFrame(){
        myParentWindow.remTab( tabbedPaneIndex );
        expandedFrame = null;
        myParentWindow.remFrame(listIndex);
        //stop();
        stopRecording();
        cameraThread.stop();

        System.out.println("Removendo o Frame: " +myCamsName);
        super.remFrame();
    }

    long firstTimeStamp = -1;

    /*
     * Atualiza todos os campos relacionados à imagem da camera
     */
    @Override
    public synchronized void refreshLabel( Image newImage ){
        BufferedImage _640 = null;
        if ( newImage != null){
            cameraImages1.setImage( resize(newImage, 320, 240) );
            cameraImages2.setImage( _640 = resize(newImage, 640, 480) );
            label1.repaint();
            if(label2 != null) label2.repaint();

            // Inserir a imagem atual no arquivo de vídeo
            if(setRecord){
                BufferedImage worksWithXugglerBufferedImage =
                        convertToType(_640, BufferedImage.TYPE_3BYTE_BGR);
                IPacket packet = IPacket.make();
                IConverter converter = ConverterFactory
                        .createConverter(worksWithXugglerBufferedImage,
                        IPixelFormat.Type.YUV420P);

                long now = System.currentTimeMillis();
                if(firstTimeStamp == -1) firstTimeStamp = now;
                long timeStamp = (now - firstTimeStamp)*1000;

                IVideoPicture outFrame = converter.toPicture(
                        worksWithXugglerBufferedImage, timeStamp);
                outFrame.setQuality(0);
                outStreamCoder.encodeVideo(packet, outFrame, 0);

                if(packet.isComplete() ) outContainer.writePacket(packet);
            }
        }
    }

    int cont = 0;

    /*
     * Redimensiona uma imagem para o tamanho passado por parametros.
     */
    public BufferedImage resize(Image img, int width, int height) {
        img = new ImageIcon(img).getImage();
        BufferedImage resizedImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }
    
    /*
     * Código obtido de http://wiki.xuggle.com/Encoding_Video_from_a_sequence_of_Images
     * acessível em 20/10/2011.
     * Retorna uma imagem recebida convertida para um formato adequado
     * à gravação.
     */
    public BufferedImage convertToType(
            BufferedImage sourceImage, int targetType)
    {
        BufferedImage image;
        // if the source image is already the target type, return the source image
        if (sourceImage.getType() == targetType)
            image = sourceImage;
        // otherwise create a new image of the target type and draw the new image
        else{
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }
        return image;
    }

    /*
     * Janela para seleção de arquivos em formato específico
     */
    class MyFileChooser extends JFileChooser{
        private String myExtension;

        public MyFileChooser(String extension){
            myExtension = "." +extension;

            setAcceptAllFileFilterUsed(false);
            FileFilter type = new ExtensionFilter(
                    myExtension +" files", myExtension);
            addChoosableFileFilter(type);
        }

        @Override
        public void approveSelection(){
            File f = getSelectedFile();
            String path = f.getAbsolutePath();
            if(!path.endsWith(myExtension)){
                f = new File(path +myExtension);
                super.setSelectedFile(f);
            }
            if(f.exists() && getDialogType() == SAVE_DIALOG){
                int result = JOptionPane.showConfirmDialog(myParentWindow,
                        "Arquivo já existente, substituir?",
                        myCamsName,
                        JOptionPane.YES_NO_CANCEL_OPTION);
                switch(result){
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                }
            }
            super.approveSelection();
        }
    }
}
