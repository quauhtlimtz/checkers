# DAMAS CHINAS MINI-MAX ALFA-BETA PRUNING
 

## Reglas
 

* Las fichas se mueven un casillero en diagonal hacia adelante; en ningún caso pueden ser movidas hacia un casillero blanco.

* Los jugadores juegan una ficha por turno.

* Cuando una ficha tiene un espacio vacío detrás de ella y se halla adelante de una ficha enemiga, ésta puede saltar en línea recta sobre ella hacia ese espacio y “comerla”. La pieza saltada es retirada del juego.

* Se puede comer varias piezas en una sola jugada.

* El comer es obligatorio. Sin embargo, el jugador puede “pasar” y ceder su turno al oponente.

* Cuando hay varias posibilidades para comer, queda a libre elección del jugador cuál o cuáles piezas va a comer.

* Cuando una pieza llega un casillero de la primera línea del oponente, es coronada como "Rey. Esta ficha puede moverse tanto hacia adelante como hacia atrás (sólo una casilla), y saltar sobre piezas del oponente, comiéndolas. Pero como cualquier otra pieza, también puede ser comida.

* El juego termina cuando uno de los jugadores ha perdido todas sus piezas o cuando uno de los jugadores no puede mover ninguna pieza porque todas están bloqueadas. El jugador que ha hecho la última jugada es declarado ganador. También se puede terminar el juego de mutuo acuerdo, declarándose tablas o empate. Esto sucede, por ejemplo, cuando a cada uno de los jugadores le queda sólo una dama en juego.

 

## El Algoritmo

Esta aplicación se basa en el algoritmo Mini-Max con poda Alfa-Beta (propuesto en el libro “Artificial Intelligence: A Modern Approach” de Stuart Russell y Peter Norvig. http://aima.cs.berkeley.edu/) para realizar el cálculo –de manera eficiente- del mejor movimiento. Fue realizada en mayo de 2003 por alumnos del curso “Inteligencia Artificial” en Instituto Tecnológico Autónomo de México (ITAM). Si estás interesado en el código fuente del programa, envía un mail a al80257@alumnos.itam.mx.

Los niveles de dificultad señalan la profundidad que utiliza el algoritmo Alfa-Beta antes de evaluar un estado. En el nivel de dificultad igual a 1, se expanden los nodos hasta una profundidad igual a 2; en el 2, la profundidad es igual a 4; en el 3, la profundidad es igual a 6; en el 4, la profundidad es igual a 8; por último, en el 5, la profundidad es igual a 10 .

Es posible realizar una profundidad mayor a 10, sin embargo, con los recursos computacionales comunes (es decir, una PC normal) tomaría demasiado para que la computadora realizara un movimiento pues la cantidad de nodos expandidos oscilaría en algunas decenas de millones.

 

## Alfa-Beta
 

En cada llamada recursiva, se pasan 2 valores (a y b) al nodo hijo donde:
a marca la cota inferior de los valores que se van a ir buscando en la parte del árbol que queda por explorar y b la cota superior.
Si a³b no tiene sentido seguir con la búsqueda realizándose una poda a si estamos en un nodo MAX o b si estamos en un nodo MIN.
 

Condición de restricción de monotonía
 

Debe cumplirse que para toda conexión entre un nodo y sus sucesores:

 

h(nodo)  £  c + h(nodo 1) + h(nodo 2) + ... + h(nodo n)

 

donde c es el coste de la conexión.

Si  n es cualquier nodo terminal y h(n)=0, la restricción de monotonía implica que:

 

h(nodo) £ h*(nodo)

 

para cualquier nodo.

 

## Función de evaluación
 

Esta función indica qué tan bueno o malo es el estado actual de un elemento (en este caso el tablero). Es auxiliar, entre otras cosas, para evitar que sean expandidos todos los nodos de un árbol.

La función de evaluación utilizada en esta aplicación cuenta con tres elementos heurísticos que se calculan a partir de un estado (en este caso, a partir de las posiciones de las fichas del tablero).

Es de la forma fev(estado)=6R + 4M + U.

R: Diferencia de las fichas Rey de la computadora y las fichas Rey del usuario.

M: Diferencia de las fichas normales (no Rey) de la computadora y las fichas normales del usuario.

U: Resultado de los movimientos que puede realizar la computadora sin ocasionar la pérdida de su ficha menos los movimientos que puede hacer el usuario sin ocasionar la pérdida de su ficha.

Cabe señalar que esta función de evaluación no trabaja de manera adecuada en niveles donde la profundidad no es muy grande (por ejemplo, 2) ya que, para estos niveles, muchas veces es imposible lograr que algunas fichas sean Rey.

 

 
