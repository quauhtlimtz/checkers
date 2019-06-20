
import java.applet.*;
import java.awt.*;
import java.io.*;

class Casilla
{
        int tipo;
        int color;
        // s*: Destino del movimiento, -1 si no puede moverse
        // e*: Índice de la pieza eliminada cuando se realiza un movimiento
        int saltos, s[], e[];
        int movimientos, m[];

        public Casilla()
        {
                s = new int[4];
                e = new int[4];
                m = new int[4];
                tipo = color = 0;
                saltos = movimientos = 0;
                for ( int i = 0; i < 4; i++ )
                        s[i] = e[i] = m[i] = -1;
        }
}


public class Damas extends Applet implements Runnable
{

        private Thread	 m_Damas = null;

        public final int VACÍO = 0, ROJO = 1, BLANCO = 2, REYROJO = 3, REYBLANCO = 4;
        private Graphics m_Graphics;
        private Image	 m_Imágenes[];
        private int 	 m_nImagenActual;
        private int 	 m_nImgAncho  = 0;
        private int 	 m_nImgAltura = 0;
        private boolean  m_fTodasLeídas = false;

        private final Color BGColor = new Color(0,0,0);
        private Casilla tablero[];
        private boolean resaltada = false;
        private int resaltadaÍndice = -1, dragIndex = -1;
        private int turno = ROJO;
        private boolean gameover = false;
        private Point últimoPunto = new Point( -1, -1 );
        private Graphics dragGráficas;

        private int Nivel = 4;
        private int ÍndiceOrigen, ÍndiceDestino;

        private Label nivel,ganador,status;
        private Choice dificultad;
        private Button comenzar,pasar;

        public Damas()
        {
        }

        public String getAppletInfo()
        {
                return "Damas";
        }


        public void init()
        {

                this.resize( 600, 400 );
                this.setLayout( null );
		    this.setBackground(Color.black);

                nivel = new Label( "Nivel:", Label.LEFT );
                this.add( nivel );
                nivel.setFont( new Font( "Times New Roman", Font.BOLD, 15 ) );
                nivel.reshape( 420, 70, 50, 25 );

                status=new Label("Tu turno",Label.LEFT);
                this.add(status);
                status.setFont( new Font( "Times New Roman", Font.BOLD, 15 ) );
                status.reshape(420,200,300,25);

                ganador=new Label("",Label.LEFT);
                this.add(ganador);
                ganador.setFont( new Font( "Times New Roman", Font.BOLD, 15 ) );
                ganador.reshape(420,150,300,25);


                dificultad = new Choice();
                dificultad.addItem( "1(Más fácil) " );
                dificultad.addItem( "2         " );
                dificultad.addItem( "3         " );
                dificultad.addItem( "4         " );
                dificultad.addItem( "5(Más difícil)" );
                this.add( dificultad );
                dificultad.reshape( 480, 70, 100, 25 );

                pasar = new Button( "Pasar" );
                this.add( pasar );
                pasar.setFont( new Font( "Times New Roman", Font.PLAIN, 15 ) );
                pasar.reshape( 420, 260, 160, 30 );


                comenzar = new Button( "Juego nuevo" );
                this.add( comenzar );
                comenzar.setFont( new Font( "Times New Roman", Font.PLAIN, 15 ) );
                comenzar.reshape( 420, 360, 160, 30 );



                //Creo un nuevo tablero
                tablero = new Casilla[32];
                for ( int i = 0; i < 32; i++ )
                        tablero[i] = new Casilla();
        }

        public void destroy()
        {
                if (m_Damas != null)
                {
                        m_Damas.stop();
                        m_Damas = null;
                }
        }

        public void paint( Graphics g )
        {
                if (m_fTodasLeídas)
                {
                        g.drawImage( m_Imágenes[0], 0, 0, null );			//imagen tablero
                        PintarTodas();
                }
                else
                        g.drawString("Abriendo imágenes...", 10, 20);
        }

        public void start()
        {
                m_Graphics = getGraphics();
                if (m_Damas == null)
                {
                        m_Damas = new Thread(this); //Thread
                        m_Damas.start();
                }
        }

        public void stop()
        {
                if (m_Damas != null)
                {
                        m_Damas.stop();
                        m_Damas = null;
                }
        }

        private void JuegoNuevo()
        {
        		pasar.enable(true);
                resaltada = false;
                resaltadaÍndice = -1;
                dragIndex = -1;
                ÍndiceOrigen = ÍndiceDestino = -1;
                gameover = false;
                turno = ROJO;
                Nivel = dificultad.getSelectedIndex() * 2 + 2; //Aumenta de 2 en 2 hasta llegar al nivel 10
		    status.setText("Tu turno");

                int i;
                for ( i = 0; i < 12; i++ )
                {
                        tablero[i].tipo = ROJO;
                        tablero[i].color = ROJO;
                }
                for ( i = 12; i < 20; i++ )
                {
                        tablero[i].tipo = VACÍO;
                        tablero[i].color = VACÍO;
                }
                for ( i = 20; i < 32; i++ )
                {
                        tablero[i].tipo = BLANCO;
                        tablero[i].color = BLANCO;
                }
                ActualizarCasillas( tablero );
                ganador.setText("");
        }

        //Obtiene las coordenadas absolutas a partir de una posición de una pieza
        private Point grid2Pos( int i )
        {
                int x = ( ( i / 4 ) % 2 == 0
                                ? ( 3 - i % 4 ) * 100
                                : ( 3 - i % 4 ) * 100 + 50 );
                int y = ( 7 - i / 4 ) * 50;
                return new Point( x, y );
        }

        //Obtiene una posición de una pieza a partir de una coordenada absoluta
        //(0 - 31, -1 si se proporciona una coordenada inválida
        private int pos2Grid( int x, int y )
        {
                int renglón = ( 399 - y ) / 50;
                int columna = ( 399 - x ) / 50;
                if ( renglón % 2 == columna % 2 || renglón < 0 || renglón > 7 || columna < 0 || columna > 7 )
                        return -1;
                else
                        return renglón * 4 + columna / 2;
        }

        //Regresa "true" si la 'i' pieza puede comer
        private boolean ActualizaciónSalto( Casilla estado[], int i )
        {
                if ( i == -1 )
                        return false;

                estado[i].saltos = 0;
                estado[i].s[0] = estado[i].e[0] = -1;
                estado[i].s[1] = estado[i].e[1] = -1;
                estado[i].s[2] = estado[i].e[2] = -1;
                estado[i].s[3] = estado[i].e[3] = -1;

                if ( estado[i].tipo == VACÍO )
                        return false;

                int tipo = estado[i].tipo, c = estado[i].color;
                if ( tipo == ROJO || tipo == REYROJO || tipo == REYBLANCO )
                {
                        switch( i )
                        {
                        case 3 : case 11 : case 19 :
                                if ( estado[i+4].color != c && estado[i+4].tipo != VACÍO &&
                                         estado[i+7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 7;
                                        estado[i].e[0] = i + 4;
                                }
                                break;
                        case 0 : case 8 : case 16 :
                                if ( estado[i+5].color != c && estado[i+5].tipo != VACÍO &&
                                         estado[i+9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 9;
                                        estado[i].e[0] = i + 5;
                                }
                                break;
                        case 7 : case 15 : case 23 :
                                if ( estado[i+3].color != c && estado[i+3].tipo != VACÍO &&
                                         estado[i+7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 7;
                                        estado[i].e[0] = i + 3;
                                }
                                break;
                        case 4 : case 12 : case 20 :
                                if ( estado[i+4].color != c && estado[i+4].tipo != VACÍO &&
                                         estado[i+9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 9;
                                        estado[i].e[0] = i + 4;
                                }
                                break;
                        case 24 : case 25 : case 26 : case 27 :
                        case 28 : case 29 : case 30 : case 31 :
                                break;
                        case 1 : case 2 :
                        case 9 : case 10 :
                        case 17 : case 18 :
                                if ( estado[i+4].color != c && estado[i+4].tipo != VACÍO &&
                                         estado[i+7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 7;
                                        estado[i].e[0] = i + 4;
                                }
                                if ( estado[i+5].color != c && estado[i+5].tipo != VACÍO &&
                                         estado[i+9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[1] = i + 9;
                                        estado[i].e[1] = i + 5;
                                }
                                break;
                        case 5 : case 6 :
                        case 13 : case 14 :
                        case 21 : case 22 :
                                if ( estado[i+3].color != c && estado[i+3].tipo != VACÍO &&
                                         estado[i+7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[0] = i + 7;
                                        estado[i].e[0] = i + 3;
                                }
                                if ( estado[i+4].color != c && estado[i+4].tipo != VACÍO &&
                                         estado[i+9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[1] = i + 9;
                                        estado[i].e[1] = i + 4;
                                }
                                break;
                        }
                }
                if ( tipo == BLANCO || tipo == REYROJO || tipo == REYBLANCO )
                {
                        switch( i )
                        {
                        case 28 : case 20 : case 12 :
                                if ( estado[i-4].color != c && estado[i-4].tipo != VACÍO &&
                                         estado[i-7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 7;
                                        estado[i].e[2] = i - 4;
                                }
                                break;
                        case 31 : case 23 : case 15 :
                                if ( estado[i-5].color != c && estado[i-5].tipo != VACÍO &&
                                         estado[i-9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 9;
                                        estado[i].e[2] = i - 5;
                                }
                                break;
                        case 24 : case 16 : case 8 :
                                if ( estado[i-3].color != c && estado[i-3].tipo != VACÍO &&
                                         estado[i-7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 7;
                                        estado[i].e[2] = i - 3;
                                }
                                break;
                        case 27 : case 19 : case 11 :
                                if ( estado[i-4].color != c && estado[i-4].tipo != VACÍO &&
                                         estado[i-9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 9;
                                        estado[i].e[2] = i - 4;
                                }
                                break;
                        case 0 : case 1 : case 2 : case 3 :
                        case 4 : case 5 : case 6 : case 7 :
                                break;
                        case 30 : case 29 :
                        case 22 : case 21 :
                        case 14 : case 13 :
                                if ( estado[i-4].color != c && estado[i-4].tipo != VACÍO &&
                                         estado[i-7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 7;
                                        estado[i].e[2] = i - 4;
                                }
                                if ( estado[i-5].color != c && estado[i-5].tipo != VACÍO &&
                                         estado[i-9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[3] = i - 9;
                                        estado[i].e[3] = i - 5;
                                }
                                break;
                        case 26 : case 25 :
                        case 18 : case 17 :
                        case 10 : case 9 :
                                if ( estado[i-3].color != c && estado[i-3].tipo != VACÍO &&
                                         estado[i-7].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[2] = i - 7;
                                        estado[i].e[2] = i - 3;
                                }
                                if ( estado[i-4].color != c && estado[i-4].tipo != VACÍO &&
                                         estado[i-9].tipo == VACÍO )
                                {
                                        estado[i].saltos++;
                                        estado[i].s[3] = i - 9;
                                        estado[i].e[3] = i - 4;
                                }
                                break;
                        }
                }

                return ( estado[i].saltos > 0 );
        }

        //Regresa "true" si la pieza se puede mover o puede comer
        private boolean ActualizaciónMovimiento( Casilla estado[], int i )
        {
                if ( i == -1 )
                        return false;

                estado[i].movimientos = 0;
                estado[i].m[0] = estado[i].m[1] = estado[i].m[2] = estado[i].m[3] = -1;

                if ( estado[i].tipo == VACÍO )
                        return false;

                if ( estado[i].saltos > 0 )
                        return true;
                else
                        // Si algún otro puede saltar, debe saltar
                        for ( int p = 0; p < 32; p++ )
                                if ( estado[p].color == estado[i].color && estado[p].saltos > 0 )
                                        return false;

                int tipo = estado[i].tipo;
                if ( tipo == ROJO || tipo == REYROJO || tipo == REYBLANCO )
                {
                        switch( i )
                        {
                        case 3 : case 11 : case 19 : case 27 :
                        case 4 : case 12 : case 20 :
                                if ( estado[i+4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[0] = i + 4;
                                }
                                break;
                        case 28 : case 29 : case 30 : case 31 :
                                break;
                        case 0 : case 1 : case 2 :
                        case 8 : case 9 : case 10 :
                        case 16 : case 17 : case 18 :
                        case 24 : case 25 : case 26 :
                                if ( estado[i+4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[0] = i + 4;
                                }
                                if ( estado[i+5].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[1] = i + 5;
                                }
                                break;
                        case 5 : case 6 : case 7 :
                        case 13 : case 14 : case 15 :
                        case 21 : case 22 : case 23 :
                                if ( estado[i+3].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[0] = i + 3;
                                }
                                if ( estado[i+4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[1] = i + 4;
                                }
                                break;
                        }
                }
                if ( tipo == BLANCO || tipo == REYROJO || tipo == REYBLANCO )
                {
                        switch( i )
                        {
                        case 28 : case 20 : case 12 : case 4 :
                        case 27 : case 19 : case 11 :
                                if ( estado[i-4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[2] = i - 4;
                                }
                                break;
                        case 0 : case 1 : case 2 : case 3 :
                                break;
                        case 31 : case 30 : case 29 :
                        case 23 : case 22 : case 21 :
                        case 15 : case 14 : case 13 :
                        case 7 : case 6 : case 5 :
                                if ( estado[i-4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[2] = i - 4;
                                }
                                if ( estado[i-5].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[3] = i - 5;
                                }
                                break;
                        case 26 : case 25 : case 24 :
                        case 18 : case 17 : case 16 :
                        case 10 : case 9 : case 8 :
                                if ( estado[i-3].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[2] = i - 3;
                                }
                                if ( estado[i-4].tipo == VACÍO )
                                {
                                        estado[i].movimientos++;
                                        estado[i].m[3] = i - 4;
                                }
                                break;
                        }
                }

                return ( estado[i].movimientos > 0 );
        }

        //Actualiza las casillas de un tablero
        private void ActualizarCasillas( Casilla estado[] )
        {
                int i;

                for ( i = 0; i < 4; i++ )
                        if ( estado[i].tipo == BLANCO )
                                estado[i].tipo = REYBLANCO;
                for ( i = 28; i < 32; i++ )
                        if ( estado[i].tipo == ROJO )
                                estado[i].tipo = REYROJO;

                for ( i = 0; i < 32; i++ )
                        ActualizaciónSalto( estado, i );
                for ( i = 0; i < 32; i++ )
                        ActualizaciónMovimiento( estado, i );
        }

        private boolean Movible( Casilla estado[], int color, int i )
        {
                if ( i == -1 || estado[i].color != color ||
                        ( estado[i].movimientos == 0 && estado[i].saltos == 0 ) )
                        return false;
                else
                        return true;
        }
        //Si algún jugador ha perdido todas sus piezas o si ya no puede mover, entonces
        //pierde el juego.
        private boolean Terminado( Casilla estado[], int turno )
        {
                int num = 0, movimientos = 0;
                for ( int i = 0; i < 32; i++ )
                {
                        if ( estado[i].color == turno )
                        {
                                num ++;
                                if ( estado[i].saltos > 0 || estado[i].movimientos > 0 )
                                        movimientos++;
                        }
                }

                if ( num == 0 || movimientos == 0 )
                        return true;
                else
                        return false;
        }

        private void eliminarPieza( int i )
        {
                Point p = grid2Pos( i );
                m_Graphics.setColor( BGColor );
                m_Graphics.fillOval( p.x + 5, p.y + 5, 40, 40 );
        }

        private void PintarPieza( int i )
        {
                Point p = grid2Pos( i );
                int tipo = tablero[i].tipo;
                if ( tipo == ROJO )
                        m_Graphics.drawImage( m_Imágenes[1], p.x + 7, p.y + 7, Color.black,null );
                else if ( tipo == BLANCO )
                        m_Graphics.drawImage( m_Imágenes[2], p.x + 7, p.y + 7, Color.black,null );
                else if ( tipo == REYROJO )
                        m_Graphics.drawImage( m_Imágenes[3], p.x + 7, p.y + 7, Color.black,null  );
                else if ( tipo == REYBLANCO )
                        m_Graphics.drawImage( m_Imágenes[4], p.x + 7, p.y + 7, Color.black,null );
                else if ( tipo == VACÍO )
                        eliminarPieza( i );
        }

        private void PintarTodas()
        {
                for ( int i = 0; i < 32; i++ )
                        PintarPieza( i );
        }


        public void run()
        {
                JuegoNuevo();
                m_nImagenActual = 0;

        if (!m_fTodasLeídas)
                {
                        m_Imágenes = new Image[5];
                    String strImage;

                        MediaTracker tracker = new MediaTracker(this);

                        strImage = "tablero.gif";
                        m_Imágenes[0] = getImage(getDocumentBase(), strImage);
            tracker.addImage(m_Imágenes[0], 0);

                        strImage = "rojo.gif";
                        m_Imágenes[1] = getImage(getDocumentBase(), strImage);
            tracker.addImage(m_Imágenes[1], 0);

                        strImage = "blanco.gif";
                        m_Imágenes[2] = getImage(getDocumentBase(), strImage);
            tracker.addImage(m_Imágenes[2], 0);

                        strImage = "reyrojo.gif";
                        m_Imágenes[3] = getImage(getDocumentBase(), strImage);
            tracker.addImage(m_Imágenes[3], 0);

                        strImage = "reyblanco.gif";
                        m_Imágenes[4] = getImage(getDocumentBase(), strImage);
            tracker.addImage(m_Imágenes[4], 0);

                        // Espera hasta que se leen todas las imágenes
                    //------------------------------------------------------------------
                        try
                        {
                                tracker.waitForAll();
                                m_fTodasLeídas = !tracker.isErrorAny();
                        }
                        catch (InterruptedException ex)
                        {
                        }

                        if (!m_fTodasLeídas)
                        {
                            stop();
                            m_Graphics.drawString("Error al tratar de leer las imágenes!", 10, 40);
                            return;
                        }
        }
                repaint();
                m_Damas.suspend();

                while (true)
                {
                        if ( gameover )
                        {
                          if(turno==ROJO)
                            ganador.setText("GANADOR: PC");
                          else 
							ganador.setText("GANADOR: Usuario");
				  		this.stop();
				    	  pasar.disable();
                        }
                        else if ( turno == BLANCO )
                        {
                                Nivel = dificultad.getSelectedIndex() * 2 + 2;
					  status.setText("Pensando...");
					  pasar.disable();
                                comenzar.disable();
                                dificultad.disable();
                                PasoSiguiente();
					  status.setText("Tu turno");
                                dificultad.enable();
                                comenzar.enable();
					  pasar.enable();
                                //El mejor movimiento fue almacenado en las variables
                                //'ÍndiceOrigen' y 'ÍndiceDestino'
                                if ( tablero[ÍndiceOrigen].m[0] == ÍndiceDestino ||
                                         tablero[ÍndiceOrigen].m[1] == ÍndiceDestino ||
                                         tablero[ÍndiceOrigen].m[2] == ÍndiceDestino ||
                                         tablero[ÍndiceOrigen].m[3] == ÍndiceDestino )
                                {
                                        tablero[ÍndiceDestino].tipo = tablero[ÍndiceOrigen].tipo;
                                        tablero[ÍndiceDestino].color = tablero[ÍndiceOrigen].color;
                                        tablero[ÍndiceOrigen].tipo = tablero[ÍndiceOrigen].color = VACÍO;
                                        ActualizarCasillas( tablero );
                                        eliminarPieza( ÍndiceOrigen );
                                        PintarPieza( ÍndiceDestino );
                                }
                                else
                                {
                                        for ( int i = 0; i < 4; i++ )
                                        {
                                                if ( tablero[ÍndiceOrigen].s[i] == ÍndiceDestino )
                                                {
                                                        int delIndex = tablero[ÍndiceOrigen].e[i];
                                                        tablero[ÍndiceDestino].tipo = tablero[ÍndiceOrigen].tipo;
                                                        tablero[ÍndiceDestino].color = tablero[ÍndiceOrigen].color;
                                                        tablero[ÍndiceOrigen].tipo = tablero[ÍndiceOrigen].color = VACÍO;
                                                        tablero[delIndex].tipo = tablero[delIndex].color = VACÍO;
                                                        ActualizarCasillas( tablero );

                                                        eliminarPieza( ÍndiceOrigen );
                                                        eliminarPieza( delIndex );
                                                        PintarPieza( ÍndiceDestino );

                                                        // Handling continuelly jump.
                                                        while( tablero[ÍndiceDestino].saltos > 0 )
                                                        {
                                                                for ( int m = 0; m < 4; m++ )
                                                                {
                                                                        if ( tablero[ÍndiceDestino].s[m] >= 0 )
                                                                        {
                                                                                int newDest = tablero[ÍndiceDestino].s[m];
                                                                                int newDel = tablero[ÍndiceDestino].e[m];
                                                                                tablero[newDest].tipo = tablero[ÍndiceDestino].tipo;
                                                                                tablero[newDest].color = tablero[ÍndiceDestino].color;
                                                                                tablero[ÍndiceDestino].tipo = tablero[ÍndiceDestino].color = VACÍO;
                                                                                tablero[newDel].tipo = tablero[newDel].color = VACÍO;
                                                                                ActualizarCasillas( tablero );

                                                                                eliminarPieza( ÍndiceDestino );
                                                                                eliminarPieza( newDel );
                                                                                PintarPieza( newDest );
                                                                                ÍndiceDestino = newDest;

                                                                                break;
                                                                        }
                                                                }
                                                        }

                                                        break;
                                                }
                                        }
                                }




                                turno = ROJO;
                                if ( Terminado( tablero, ROJO ) )
                                {
                                        gameover = true;
                                }
                                else
                                        m_Damas.suspend();
                        }
                }
        }

        public boolean action( Event e, Object o )
        {
                if ( o.equals( "Juego nuevo" ) )
                {
                        if (m_Damas != null)
                                m_Damas.stop();
                        m_Damas = new Thread(this);
                        m_Damas.start();
                }
		    if ( o.equals("Pasar"))
		    {
			turno=BLANCO;
			m_Damas.resume();
		    }
                return true;
        }

        public boolean mouseDown( Event evt, int x, int y )
        {
                if ( turno == BLANCO ) //Las blancas es la PC
                        return false;

                int index = pos2Grid( x, y );
                if ( Movible( tablero, turno, index ) )
                {
                        dragIndex = index;

                        eliminarPieza( index );
                        dragGráficas = getGraphics();
                        dragGráficas.setXORMode(Color.black); //Para que no se pinte la línea al arrastrar una ficha
                        if ( tablero[dragIndex].tipo == ROJO )
                                dragGráficas.drawImage( m_Imágenes[1], x - 18, y - 18, null );
                        else if ( tablero[dragIndex].tipo == REYROJO )
                                dragGráficas.drawImage( m_Imágenes[3], x - 18, y - 18, null );
                        últimoPunto.x = x - 18; últimoPunto.y = y - 18;
                }
                return false;
        }

        public boolean mouseUp( Event evt, int x, int y )
        {
                //Calcula la utilidad del estado actual.

                if ( dragIndex >= 0 )
                {
                        if ( tablero[dragIndex].tipo == ROJO )
                                dragGráficas.drawImage( m_Imágenes[1], últimoPunto.x, últimoPunto.y, null );
                        else if ( tablero[dragIndex].tipo == REYROJO )
                                dragGráficas.drawImage( m_Imágenes[3], últimoPunto.x, últimoPunto.y, null );
                        últimoPunto.x = últimoPunto.y = -1;
                        PintarPieza( dragIndex );
                }

                int dest = pos2Grid( x, y );
                if ( dest != -1 && dragIndex >= 0 )
                {
                        if ( tablero[dragIndex].m[0] == dest ||
                                 tablero[dragIndex].m[1] == dest ||
                                 tablero[dragIndex].m[2] == dest ||
                                 tablero[dragIndex].m[3] == dest )
                        {
                                tablero[dest].tipo = tablero[dragIndex].tipo;
                                tablero[dest].color = tablero[dragIndex].color;
                                tablero[dragIndex].tipo = tablero[dragIndex].color = VACÍO;
                                ActualizarCasillas( tablero );

                                eliminarPieza( dragIndex );
                                PintarPieza( dest );

                                if ( Terminado( tablero, BLANCO ) )
                                {
                                        gameover = true;
                                }
                                turno = BLANCO;
                                m_Damas.resume();
                        }
                        else
                        {
                                for ( int i = 0; i < 4; i++ )
                                {
                                        if ( tablero[dragIndex].s[i] == dest )
                                        {
                                                int delIndex = tablero[dragIndex].e[i];
                                                tablero[dest].tipo = tablero[dragIndex].tipo;
                                                tablero[dest].color = tablero[dragIndex].color;
                                                tablero[dragIndex].tipo = tablero[dragIndex].color = VACÍO;
                                                tablero[delIndex].tipo = tablero[delIndex].color = VACÍO;
                                                ActualizarCasillas( tablero );

                                                eliminarPieza( dragIndex );
                                                eliminarPieza( delIndex );
                                                PintarPieza( dest );

                                                // Handling continuelly jump.
                                                while( tablero[dest].saltos > 0 )
                                                {
                                                        for ( int m = 0; m < 4; m++ )
                                                        {
                                                                if ( tablero[dest].s[m] >= 0 )
                                                                {
                                                                        int newDest = tablero[dest].s[m];
                                                                        int newDel = tablero[dest].e[m];
                                                                        tablero[newDest].tipo = tablero[dest].tipo;
                                                                        tablero[newDest].color = tablero[dest].color;
                                                                        tablero[dest].tipo = tablero[dest].color = VACÍO;
                                                                        tablero[newDel].tipo = tablero[newDel].color = VACÍO;
                                                                        ActualizarCasillas( tablero );

                                                                        eliminarPieza( dest );
                                                                        eliminarPieza( newDel );
                                                                        PintarPieza( newDest );
                                                                        dest = newDest;

                                                                        break;
                                                                }
                                                        }
                                                }


                                                if ( Terminado( tablero, BLANCO ) )
                                                {
                                                        gameover = true;
                                                }
                                                turno = BLANCO;
                                                m_Damas.resume();
                                                break;
                                        }
                                }
                        }
                }

                dragIndex = -1;
                return false;
        }

        public boolean mouseDrag( Event evt, int x, int y )
        {
                if ( dragIndex >= 0 )
                {
                        if ( tablero[dragIndex].tipo == ROJO )
                        {
                                dragGráficas.drawImage( m_Imágenes[1], últimoPunto.x, últimoPunto.y, null );
                                dragGráficas.drawImage( m_Imágenes[1], x - 18, y - 18, null );
                        }
                        else if ( tablero[dragIndex].tipo == REYROJO )
                        {
                                dragGráficas.drawImage( m_Imágenes[3], últimoPunto.x, últimoPunto.y, null );
                                dragGráficas.drawImage( m_Imágenes[3], x - 18, y - 18, null );
                        }
                        últimoPunto.x = x - 18; últimoPunto.y = y - 18;
                }
                return false;
        }


        private void CopiarEstado( Casilla src[], Casilla dest[] )
        {
                for ( int i = 0; i < 32; i++ )
                {
                        dest[i].tipo = src[i].tipo;
                        dest[i].color = src[i].color;
                        dest[i].saltos = src[i].saltos;
                        dest[i].movimientos = src[i].movimientos;
                        for ( int k = 0; k < 4; k++ )
                        {
                                dest[i].s[k] = src[i].s[k];
                                dest[i].e[k] = src[i].e[k];
                                dest[i].m[k] = src[i].m[k];
                        }
                }
        }

        //Regresa la evaluación de un tablero.
        //Se ejecuta una vez que se cumple con la profundidad elegida.
		//Recibe el arreglo de casillas (o "tablero")
        /*private float evaluar( Casilla estado[] , int i)
        {
                int res = 0;

                for ( int i = 0; i < 32; i++ )
                {
                        if ( estado[i].tipo == BLANCO )
                                res ++;
                        else if ( estado[i].tipo == REYBLANCO )
                                res += 2;
                        else if ( estado[i].tipo == ROJO )
                                res --;
                        else if ( estado[i].tipo == REYROJO )
                                res -= 2;
                }

                return res;
        }*/

        private float evaluar(Casilla estado[])
        {
        	int ReyesPc=0,ReyesYo=0,
        		NormalesPc=0,NormalesYo=0,
        		MovilidadPc=0,MovilidadYo=0;
        	float res;
        	for (int i=0;i<32;i++)
        	{
        		if( estado[i].tipo==REYBLANCO)
        			ReyesPc++;
        		else if(estado[i].tipo==REYROJO)
        			ReyesYo++;
        		if (estado[i].color==BLANCO)
        		{
        			MovilidadPc+=estado[i].movimientos+
        								estado[i].saltos;
        			NormalesPc++;
        		}
        		else if(estado[i].color==ROJO)
        		{
        			MovilidadYo+=estado[i].movimientos+
        								estado[i].saltos;
        			NormalesYo++;
        		}
        	}
			res=6*(ReyesPc-ReyesYo)+
				4*(NormalesPc-NormalesYo)+
				MovilidadPc-MovilidadYo;
        	return res;
        }

        //Actualiza el paso siguiente de la posición en ÍndiceOrigen y ÍndiceDestino
        private float PasoSiguiente()
        {
                return ValorMax( tablero, 0, -1000, 1000 );
        }


        // Algoritmo alpha-beta
        //Russell & Norvig
        private float ValorMax( Casilla estado[], int steps, float alpha, float beta )
        {
                if ( Terminado( estado, BLANCO ) )
                        return -500;
                if ( steps >= Nivel )
                        return evaluar( estado );

                int i;

                Casilla[] newState = new Casilla[32];
                for ( i = 0; i < 32; i++ )
                        newState[i] = new Casilla();

                for ( i = 0; i < 32; i++ )
                {
                        if ( Movible( estado, BLANCO, i ) )
                        {
                                if ( estado[i].saltos > 0 ) //Saltos son las veces que puede comer
                                {
                                        for( int k = 0; k < 4; k++ )
                                        {
                                                if ( estado[i].s[k] >= 0 )
                                                {
                                                // Este es un posible movimiento

                                                        CopiarEstado( estado, newState );
                                                        // Prepara un nuevo estado
                                                        int del = newState[i].e[k];
                                                        int src = i, dest = newState[i].s[k];
                                                        newState[dest].tipo = newState[src].tipo;
                                                        newState[dest].color = newState[src].color;
                                                        newState[src].tipo = newState[src].color = VACÍO;
                                                        newState[del].tipo = newState[del].color = VACÍO;
                                                        ActualizarCasillas( newState );

                                                        // Handling continuelly jump.
                                                        int oldDest = dest;
                                                        while( newState[oldDest].saltos > 0 )
                                                        {
                                                                for ( int m = 0; m < 4; m++ )
                                                                {
                                                                        if ( newState[oldDest].s[m] >= 0 )
                                                                        {
                                                                                int newDest = newState[oldDest].s[m];
                                                                                int newDel = newState[oldDest].e[m];
                                                                                newState[newDest].tipo = newState[oldDest].tipo;
                                                                                newState[newDest].color = newState[oldDest].color;
                                                                                newState[oldDest].tipo = newState[oldDest].color = VACÍO;
                                                                                newState[newDel].tipo = newState[newDel].color = VACÍO;
                                                                                oldDest = newDest;
                                                                                ActualizarCasillas( newState );
                                                                                break;
                                                                        }
                                                                }
                                                        }

                                                        float a = ValorMin( newState, steps + 1, alpha, beta );
                                                        if ( a > alpha )
                                                        {
                                                                alpha = a;
                                                                if ( steps == 0 )
                                                                {
                                                                        ÍndiceOrigen = src;
                                                                        ÍndiceDestino = dest;
                                                                }
                                                        }
                                                        if ( steps > 0 && alpha >= beta )
                                                                return beta;
                                                }
                                        }
                                }
                                else if ( estado[i].movimientos > 0 ) //Ahora checa los movimientos
                                //Si entra aquí es porque no pudo comer pero sí mover
                                {
                                        for( int k = 0; k < 4; k++ )
                                        {
                                                if ( estado[i].m[k] >= 0 )
                                                {
                                                // Este es un posible movimiento

                                                        CopiarEstado( estado, newState );
                                                        // Prepara un estado nuevo
                                                        int src = i, dest = newState[i].m[k];
                                                        newState[dest].tipo = newState[src].tipo;
                                                        newState[dest].color = newState[src].color;
                                                        newState[src].tipo = newState[src].color = VACÍO;
                                                        ActualizarCasillas( newState );

                                                        float a = ValorMin( newState, steps + 1, alpha, beta );
                                                        if ( a > alpha )
                                                        {
                                                                alpha = a;
                                                                if ( steps == 0 )
                                                                {
                                                                        ÍndiceOrigen = src;
                                                                        ÍndiceDestino = dest;
                                                                }
                                                        }
                                                        if ( steps > 0 && alpha >= beta )
                                                                return beta;
                                                }
                                        }
                                }
                        }
                }
                return alpha;
        }

        private float ValorMin( Casilla estado[], int steps, float alpha, float beta )
        {
                if ( Terminado( estado, ROJO ) )
                        return 500;
                if ( steps >= Nivel )
                        return evaluar( estado );

                int i;

                Casilla[] newState = new Casilla[32];
                for ( i = 0; i < 32; i++ )
                        newState[i] = new Casilla();

                for ( i = 0; i < 32; i++ )
                {
                        if ( Movible( estado, ROJO, i ) )
                        {
                                if ( estado[i].saltos > 0 )
                                {
                                        for( int k = 0; k < 4; k++ )
                                        {
                                                if ( estado[i].s[k] >= 0 )
                                                {
                                                // Este es un posible movimiento

                                                        CopiarEstado( estado, newState );
                                                        // Prepara un estado nuevo
                                                        int del = newState[i].e[k];
                                                        int src = i, dest = newState[i].s[k];
                                                        newState[dest].tipo = newState[src].tipo;
                                                        newState[dest].color = newState[src].color;
                                                        newState[src].tipo = newState[src].color = VACÍO;
                                                        newState[del].tipo = newState[del].color = VACÍO;
                                                        ActualizarCasillas( newState );

                                                        // Handling continuelly jump.
                                                        int oldDest = dest;
                                                        while( newState[oldDest].saltos > 0 )
                                                        {
                                                                for ( int m = 0; m < 4; m++ )
                                                                {
                                                                        if ( newState[oldDest].s[m] >= 0 )
                                                                        {
                                                                                int newDest = newState[oldDest].s[m];
                                                                                int newDel = newState[oldDest].e[m];
                                                                                newState[newDest].tipo = newState[oldDest].tipo;
                                                                                newState[newDest].color = newState[oldDest].color;
                                                                                newState[oldDest].tipo = newState[oldDest].color = VACÍO;
                                                                                newState[newDel].tipo = newState[newDel].color = VACÍO;
                                                                                oldDest = newDest;
                                                                                ActualizarCasillas( newState );
                                                                                break;
                                                                        }
                                                                }
                                                        }

                                                        float b = ValorMax( newState, steps + 1, alpha, beta );
                                                        if ( b < beta )
                                                                beta = b;
                                                        if ( beta <= alpha )
                                                                return alpha;
                                                }
                                        }
                                }
                                else if ( estado[i].movimientos > 0 )
                                {
                                        for( int k = 0; k < 4; k++ )
                                        {
                                                if ( estado[i].m[k] >= 0 )
                                                {
                                                // Este es un posible movimiento

                                                        CopiarEstado( estado, newState );
                                                        // Prepara un nuevo movimiento
                                                        int src = i, dest = newState[i].m[k];
                                                        newState[dest].tipo = newState[src].tipo;
                                                        newState[dest].color = newState[src].color;
                                                        newState[src].tipo = newState[src].color = VACÍO;
                                                        ActualizarCasillas( newState );

                                                        float b = ValorMax( newState, steps + 1, alpha, beta );
                                                        if ( b < beta )
                                                                beta = b;
                                                        if ( beta <= alpha )
                                                                return alpha;
                                                }
                                        }
                                }
                        }
                }
                return beta;
        }

 private void Log(String Dato)
 {
  try{
    /*String fileName = "Log.txt" ;
    FileWriter writer = new FileWriter(fileName,true);
    writer.write("\n"+(new java.util.Date().toString())+" "+Dato);
    writer.close();*/
  }catch(Exception e)
  {
    Log(e.getMessage());
  }
 }


}
