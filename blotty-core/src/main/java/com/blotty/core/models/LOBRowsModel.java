package com.blotty.core.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.blotty.core.commons.exceptions.RowsModelException;
import com.blotty.core.models.LOBRowsModel.StorageManager.StorageEntry;
import com.blotty.core.types.IGenericField;

public class LOBRowsModel implements IRowsModel {

	private final ColumnsModel columnsModel;

	private final Map<IGenericField,RowDump> rowsByKey = new HashMap<>();
	private final List<RowDump> rows = new ArrayList<>();
		
	static long counter = 0;
	
	private static final Map<IGenericField,RowDump> storageManagerLUT= new HashMap<>();
	private static final StorageManager storageManager = new StorageManager();
	private static StorageEntry currentStorage;
	static {
		try {
			currentStorage = storageManager.createNewStorage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public LOBRowsModel(ColumnsModel columnsModel) throws IOException {
		this.columnsModel = columnsModel;
	}
	
	private static byte[] temporaryBuff = new byte[ 1024 * 1024 * 8]; // 8Kb
	
	class RowDump {	
		private final short storageId;
		private final int[] startBuffPos;
		private final int[] endBuffPos;
		
		public RowDump(Row row, StorageEntry storageEntry ) throws IOException {
			this.startBuffPos = new int[row.getFields().length];
			this.endBuffPos = new int[row.getFields().length];
			this.storageId = storageEntry.id;
			storeRow( row, storageEntry );
		}

		private void storeRow( Row row, StorageEntry storageEntry ) throws IOException {
			int i = 0;
			for ( IGenericField field : row.getFields() ) {
				startBuffPos[i] = storageEntry.position();
				int len = storageEntry.store( field );
				if ( len>=0 ) {
					endBuffPos[i] = startBuffPos[i] + len;
				} else {
					throw new IOException("No space left");
				}
				i++;
			}
			if ( (++counter)%1000==0 ) {
				System.out.println("Stored:"+counter);
			}
		}

		public Row loadRow() throws Exception {
			StorageEntry entry = storageManager.getStorage( this.storageId );
			
			IGenericField[] fields = new IGenericField[startBuffPos.length];
			
			for ( int i=0; i<startBuffPos.length; i++ ) {
				int startPos = startBuffPos[i];
				int endPos = endBuffPos[i];		
				fields[i] = (IGenericField) entry.load(startPos, temporaryBuff, endPos-startPos);
			}
			return new Row(fields);
		}
	}

	private Map<IGenericField,StorageEntry> storedEntries = new HashMap<>();
	
	@Override
	public IRowsModel addRow( Row row ) throws RowsModelException {	
		IGenericField rowKey = row.getFields()[columnsModel.getKey().getId()];
		
		RowDump rowDump = storageManagerLUT.get(rowKey);
		if ( rowDump==null ) {
			try {
				rowDump = new RowDump(row, currentStorage);
			} catch ( BufferOverflowException ex ) {
				try {
					currentStorage = storageManager.createNewStorage();
					rowDump = new RowDump(row, currentStorage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return this;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return this;
			}	
			storageManagerLUT.put(rowKey, rowDump);
		}
			
		RowDump prev = this.rowsByKey.put(rowKey, rowDump);
		if ( prev!=null ) {
			throw new RowsModelException(String.format("Duplicated key:%s", columnsModel.getKey()));
		}
		this.rows.add(rowDump);
		
		return this;
	}	
	
	@Override
	public int getRowsCount() {
		return rows.size();
	}

	@Override
	public List<Row> getAllRows() {
		int i = 0;
		
		List<Row> loadedRows = new ArrayList<>();
		for ( RowDump r : rows ) {
			try {
				loadedRows.add(r.loadRow());
				
				if ( i==100_000 ) {
					break;
				}
				i++;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return loadedRows;
	}

	@Override
	public Row getRow(int position) {
		try {
			return rows.get(position).loadRow();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Row getRow(IGenericField key) {
		try {
			return rowsByKey.get(key).loadRow();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	static class StorageManager {

		private final int storageSize = 1024 * 1024 * 1024;	// 1024MB
				
		public class StorageEntry {
			public final byte id;
			public final MappedByteBuffer buffer;
			
			public StorageEntry(byte id, MappedByteBuffer buffer) {
				super();
				this.id = id;
				this.buffer = buffer;
			}
			
			public int position() {
				return buffer.position();
			}
			
			public int store( Object obj ) {
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
						ObjectOutputStream oos = new ObjectOutputStream(bos)) {

					oos.writeObject(obj);
					byte []buff = bos.toByteArray();

					buffer.put( buff );
					return buff.length;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return -1;
			}
			
			public Object load( int startPos, byte[] buff, int len) throws IOException, ClassNotFoundException {
				buffer.get(startPos,buff,0,len);
				
				try (ByteArrayInputStream bis = new ByteArrayInputStream(buff);
						ObjectInputStream ois = new ObjectInputStream(bis)) {
					return ois.readObject();
				}
			}
		}
		
		private AtomicInteger storageCounter = new AtomicInteger(-1);
		private List<StorageEntry> storage = new ArrayList<>();
				
		public StorageEntry createNewStorage() throws IOException {
			int id = storageCounter.incrementAndGet();
			RandomAccessFile file = new RandomAccessFile("storage_"+id+".data", "rw");
			FileChannel channel = file.getChannel();
			
			StorageEntry e = new StorageEntry(
				(byte)id,	
				channel.map(FileChannel.MapMode.READ_WRITE, 0, storageSize)
			);
			this.storage.add(id, e);
			return e;
		}
		
		public StorageEntry getStorage( int id ) throws IOException {
			return storage.get(id);
		}
	}
}
