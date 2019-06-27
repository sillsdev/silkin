/**  This class simply documents the Lexical Specification used in the DFA
	to parse Horn Clauses.  Each line of the LexicalSpec file represents one
	row of the DFA Table.  The integer at the beginning of each line is the
	state_number.  The following symbol is the token recognized by that state.
	The rest of each line consists of (Alphabit, next_state) pairs.  A next_state
	of '-99' indicates that a token has been found.
	<p>
	Note that each line ends with one blank space.
	</p> <ul>
<li>0 None WhiteSpace 0 SemiColon 1 SmLtr 3 CapLtr 4 Colon 5 LeftParen 6 RightParen 7
       Comma 8 Period 9 Bar 10 Quote 12 Digit 13 LeftAngle 14 LeftSquare 15
       RightSquare 16 Star 17 EqualSign 19 LeftCurly 20 RightCurly 21 Minus 13
       Pound 25 Percent 29
</li><li>1 None SemiColon 2
</li><li>2 comment NonLineTerm 2 LineTerm -99
</li><li>3 symbol LtrOrDig 3 LeftAngle 3 RightAngle 3 LeftCurly 3 RightCurly 3 LeftParen -99
         RightParen -99 Comma -99 Colon -99 WhiteSpace -99 Quote -99
</li><li>4 var LtrOrDig 4 Percent 4 Star 23 WhiteSpace 28 RightParen -99 Comma -99
      EqualSign -99 RightCurly -99
</li><li>5 colon Dash 11 LtrOrDig -99 WhiteSpace -99
</li><li>6 leftParen Any -99
</li><li>7 rightParen Any -99
</li><li>8 comma Any -99
</li><li>9 period Any -99
</li><li>10 bar Any -99
</li><li>11 imply Any -99
</li><li>12 string NonQuote 12 Quote -99
</li><li>13 integer Digit 13 Period 18 NonDigit -99
</li><li>14 flag LtrOrDig 14 Slash 14 RightAngle -99 QuestionMark 24 Exclamation 24
        Dash 24 WhiteSpace 26
</li><li>15 leftSquare Any -99
</li><li>16 rightSquare Any -99
</li><li>17 starName LtrOrDig 17 Star 22 RightParen -99 LeftParen -99
            LeftAngle -99 Comma -99
</li><li>18 float Digit 18 NonDigit -99
</li><li>19 equalSign Any -99
</li><li>20 leftCurly Any -99
</li><li>21 rightCurly Any -99
</li><li>22 stars Star 22 LtrOrDig -99 RightCurly -99
</li><li>23 varWithStars LtrOrDig 23 Star 23 RightParen -99 Comma -99 RightCurly -99
</li><li>24 tag LtrOrDig 24 Slash 24 QuestionMark 24 Exclamation 24 Quote 24 EqualSign 24
       WhiteSpace 24 Period 24 Dash 24 RightAngle -99
</li><li>25 pound Any -99
</li><li>26 tagWithAttributes RightAngle -99 NonSlash 26 Slash 27
</li><li>27 onePcTag RightAngle -99
</li><li>28 var WhiteSpace 28 LeftAngle -99 RightParen -99 Comma -99 EqualSign -99 RightCurly -99
</li><li>29 percent Any -99
  </li>
  * </ul>
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
*/
public class KinshipLexicalSpec  { }
