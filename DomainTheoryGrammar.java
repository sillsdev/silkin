/** This class documents the Context-Free Grammar (CFG) used in the Kinship system to
parse Domain Theories.  The CFG is embedded (hard-coded) into the class {@link ParserDomainTheory}.
<p>
DomainTheory -> LanguageName, HeaderOpts, StandardMacros, Theory.
<br/>
LanguageName -> leftParen, "languageName", comma, string, rightParen.
	First: [leftParen]    Follow: [leftParen, symbol]
<br/>
HeaderOpts ->  leftParen, HdrOpts2, HeaderOpts.
			|  \empty.
	First: [leftParen]   Follow: [symbol]
<br/>
HdrOpts2 ->  "author", comma, string, rightParen.
  		 |  "date", comma, string, rightParen.
		 |  "partial", comma, Boolean, rightParen.
		 |  "address", comma, Boolean, rightParen.
		 |  "polygamyOK", comma, Boolean, rightParen.
		 |  "citation", comma, string, rightParen.
		 |  "non_term", comma, FlagOrKinTerm, OtherFlagOrKinTerm, rightParen.
		 |  "recursiveLevels", comma, integer, rightParen.
		 |  "userDefinedProperties", UDProps, rightParen.
		 |  "synonyms", comma, TermPairs, rightParen.
		 |  "umbrellas", comma, TermListPairs, rightParen.
		 |  "overlaps", comma, TermListPairs, rightParen.
 	First: ["author", "date", "partial", "polygamyOK", "citation", "non_term", 
			"recursiveLevels", "userDefinedProperties", "umbrellas", "synonyms", "overlaps"] 	
	Follow: [leftParen, symbol]
<br/>  
Boolean ->  "true".
	 |  "false".
	First: ["true", "false"]	 Follow: [comma, rightParen]
<br/>
U_D_Props -> comma, leftParen, starName, comma, "type", comma, Type, comma, "single_value", comma, Boolean, UDP_Opts, rightParen, U_D_Props.
		  |  \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
Type ->  "integer".
     |   "float".
     |   "string".
     |   "boolean".
     |   "individual".
    First: [<all-the-above>]	 Follow: [comma]
<br/>
UDP_Opts -> comma, RestrictOrDefault, UDP_Opts.
		 |  \empty.
			First:  [comma]		Follow: [rightParen]
<br/>
RestrictOrDefault -> "restricted_to", comma, leftParen, Ints_Floats_Strings, rightParen.
				  |  "default", comma, Int_Float_String.
				  |  "max", comma, Int_Float.
				  |  "min", comma, Int_Float.
	First: ["restricted_to", "default", "max", "min"]		Follow: [comma, rightParen]
<br/>
Int_Float_String -> integer.
				 |  float.
				 |  string.
	First:  [integer, float, string]	Follow: [comma, rightParen]
<br/>
Int_Float -> integer.
		  |  float.
	First:  [integer, float]	Follow: [comma, rightParen]
<br/>
Ints_Floats_Strings -> Ints.
					|  Floats.
					|  Strings.
	First:  [integer, float, string]	Follow: [rightParen]
<br/>
Ints ->  integer, MoreInts.
<br/>
MoreInts ->  comma, integer, MoreInts.
		 |   \empty
 	 First: [comma]	 Follow: [rightParen]
<br/>
Floats ->  float, MoreFloats.
<br/>
MoreFloats ->  comma, float, MoreFloats.
		   |   \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
Strings ->  string, MoreStrings.
<br/>
MoreStrings ->  comma, string, MoreStrings.
			|   \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
StandardMacros -> KinTermDef, SignatureString, Expansion, OtherDefs.
				|	\empty.
<br/>
TermPairs -> TermPair, OtherTermPairs.
 	 First: [leftParen]	 Follow: [rightParen]
<br/>
TermPair -> leftParen, symbol, comma, symbol, rightParen.
 	 First: [leftParen]	 Follow: [comma, rightParen]
<br/>
OtherTermPairs -> comma, TermPair, OtherTermPairs.
				   |  \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
TermListPairs -> TermListPair, OtherTermListPairs.
 	 First: [leftParen]	 Follow: [rightParen]
<br/>
OtherTermListPairs -> comma, TermListPair, OtherTermListPairs.
					   |  \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
TermListPair -> leftParen, symbol, comma, TermList, rightParen.
 	 First: [leftParen]	 Follow: [rightParen]
<br/>
TermList -> leftParen, symbol, MoreTerms, rightParen.
 	 First: [leftParen]	 Follow: [rightParen]
<br/>
MoreTerms -> comma, symbol, MoreTerms.
		|  \empty.
 	 First: [comma]	 Follow: [rightParen]
<br/>
Theory ->  KinTermDef, SignatureString, Expansion, OtherDefs.
<br/>
OtherDefs ->  KinTermDef, SignatureString, Expansion, OtherDefs.
	   |  \empty.
 	 First: [symbol]	 Follow: [$]
<br/>
KinTermDef ->  ClauseHead, imply, ClauseBody, OtherBodies.
 	 First: [symbol]	 Follow: [percent $]
<br/>
ClauseHead ->  symbol, leftParen, Args, rightParen.
<br/>
Args ->  var, OtherArgs.
      |  symbol, OtherArgs.
      |  string, OtherArgs.
      |  integer, OtherArgs.
      |  float, OtherArgs.
    First: [var, symbol, string] 	 Follow: [rightParen]
<br/>
OtherArgs ->  \empty.
	   |  comma, Args.
	First: [comma]  	 Follow: [rightParen]
<br/>
ClauseBody ->  LitOrFlag, OtherLitsOrFlags, period.
	First: [symbol, flag]  	 Follow: [bar, symbol, percent, $]
<br/>
OtherLitsOrFlags ->  \empty.
    		  |  comma, LitOrFlag, OtherLitsOrFlags.
	First: [comma] 	 Follow: [period]
<br/>
OtherBodies -> bar, ClauseBody, OtherBodies.
			| \empty.
	First: [bar]     Follow: [symbol, percent, leftCurly, $]
<br/>
FlagOrKinTerm -> flag.
			  |  symbol.
	First: [flag, symbol] 	
	Follow: [comma, leftParen, symbol]
<br/>
OtherFlagOrKinTerm -> comma, OtherFlagOrKinTerm2.
  				   |  \empty.
	First: [comma] 	
	Follow: [rightParen]
<br/>
OtherFlagOrKinTerm2 -> flag, OtherFlagOrKinTerm.
   					|  symbol, OtherFlagOrKinTerm.
	First: [flag, symbol] 	
	Follow: [leftParen, symbol]
<br/>
LitOrFlag -> flag.
          |  Literal.
<br/>
Literal ->  symbol, leftParen, ArgNest, rightParen.
        |   starName, leftParen, ArgNest, rightParen.
	First: [symbol, starName]  	 Follow: [comma, period]
<br/>
ArgNest ->  var, OtherArgNest.
		 |  symbol, SymbOrLitRest, OtherArgNest.
         |  starName, leftParen, ArgNest, rightParen, OtherArgNest.
		 |  string, OtherArgNest.
         |  integer, OtherArgNest.
         |  float, OtherArgNest.
		 |  \empty.
	First: [var, symbol, starName, string, integer, float]    	 Follow: [rightParen]
<br/>
SymbOrLitRest ->  \empty.
               |  leftParen, ArgNest, rightParen.
	First: [leftParen] 	 Follow: [comma, rightParen]
<br/>
OtherArgNest ->  \empty.
              |  comma, ArgNest.
	First: [comma]  	 Follow: [rightParen]
<br/>
SignatureString -> \empty.
				 | leftCurly, string, rightCurly.
	First: [leftCurly]	Follow: [percent, symbol, $]
<br/>
Expansion -> ExpList, ExpClauseBody, Expansion.
                 |  \empty.
         First: [percent]	 Follow: [symbol, starName, $]
<br/>
ExpClauseBody ->  LitOrFlag, OtherLitsOrFlags, period.
            First: [symbol, flag]  	 Follow: [bar, symbol, percent, $]
<br/>
ExpList -> percent, ExpItem, OtherExpItems, percent, LvlStr.
    First: [percent]  Follow: [symbol, flag]
<br/>
ExpItem -> symbol, colon, integer.
        |  NegExpansion.
	First: [symbol, leftParen]		 Follow: [comma, percent, rightParen]
<br/>
OtherExpItems -> comma, ExpItem, OtherExpItems.
              |  \empty.
	First: [comma]		Follow: [percent, rightParen]
<br/>
LvlStr -> CtrSet, comma, CtrSet, comma, CtrSet, comma, leftCurly, VarOrStar, rightCurly.
	First: [symbol]		Follow: [symbol, flag]
<br/>
VarOrStar -> var, MoreVarsOrStars.
		  |  varWithStars, MoreVarsOrStars.
		  |  starName, MoreVarsOrStars.
		  |  stars, MoreVarsOrStars.
	First: [var, varWithStars, starName, stars]		Follow: [rightCurly]
<br/>
MoreVarsOrStars -> var, MoreVarsOrStars.
				|  varWithStars, MoreVarsOrStars.
				|  starName, MoreVarsOrStars.
				|  stars, MoreVarsOrStars.
				|  \empty.
	First: [var, varWithStars, starName, stars]		Follow: [rightCurly]
<br/>
CtrSet -> symbol, equal, integer.
	First: [symbol, starName]		Follow: [comma, period]
<br/>
NegExpansion -> leftParen, symbol, NegExpRest, rightParen.
	First: [leftParen]	Follow: [comma, percent, rightParen]

NegExpRest -> colon, ExpItem, OtherExpItems.
			| \empty.
	First: [colon]	Follow: [rightParen]
</p>
*/  
public abstract class DomainTheoryGrammar {}
