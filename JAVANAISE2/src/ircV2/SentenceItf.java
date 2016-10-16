package ircV2;

public interface SentenceItf {
	@JvnRW(mode=JvnRW.Mode.READ)
	public String read();
	
	@JvnRW(mode=JvnRW.Mode.WRITE)
	public void write(String s);
}
