import java.util.LinkedList;

class Database<T> {

	private LinkedList<T> items;

	public Database(){
		items = new LinkedList<T>();
	}

	public synchronized void add(T i){
		items.add(i);
		notifyAll();
	}

	public synchronized T get(int i){
		return items.get(i);
	}

	public synchronized int size(){
		return items.size();
	}

	public synchronized int indexOf(T t){
		return items.indexOf(t);
	}

	public synchronized void remove(T t){
		items.remove(t);
		notifyAll();
	}
	
}