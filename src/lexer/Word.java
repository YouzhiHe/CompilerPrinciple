package lexer;

/**
 * Created by 贺有志 on 2018/4/8.
 */
public class Word extends Token {

    public final String lexeme;
    public Word(int t,String s){
        super(t);
        lexeme = new String(s);
    }
}
