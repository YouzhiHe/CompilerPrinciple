package lexer;

/**
 * Created by 贺有志 on 2018/4/8.
 */
public class Num extends Token{
    public final int value;
    public Num(int v) {
        super(Tag.NUM);
        value = v;
    }
}
