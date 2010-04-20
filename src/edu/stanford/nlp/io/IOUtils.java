package edu.stanford.nlp.io;

import java.io.*;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.net.URL;
import java.net.URLConnection;

import edu.stanford.nlp.util.ErasureUtils;

/**
 * Helper Class for storing serialized objects to disk.
 *
 * @author Kayur Patel, Teg Grenager
 */

public class IOUtils {

  // A class of static methods
  private IOUtils() {
  }

  /**
   * Write object to a file with the specified name.
   *
   * @param o object to be written to file
   * @param filename name of the temp file
   * @throws IOException If can't write file.
   * @return File containing the object
   */
  public static File writeObjectToFile(Object o, String filename) throws IOException {
    return writeObjectToFile(o, new File(filename));
  }

    /**
     * Write an object to a specified File.
     *
     * @param o object to be written to file
     * @param file The temp File
     * @throws IOException If File cannot be written
     * @return File containing the object
     */
  public static File writeObjectToFile(Object o, File file) throws IOException {
    // file.createNewFile(); // cdm may 2005: does nothing needed
    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));
    oos.writeObject(o);
    oos.close();
    return file;
  }

  /**
   * Write object to a file with the specified name.
   *
   * @param o object to be written to file
   * @param filename name of the temp file
   *
   * @return File containing the object, or null if an exception was caught
   */
  public static File writeObjectToFileNoExceptions(Object o, String filename) {
    File file = null;
    ObjectOutputStream oos = null;
    try {
      file = new File(filename);
      // file.createNewFile(); // cdm may 2005: does nothing needed
      oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));
      oos.writeObject(o);
      oos.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (oos != null) {
        try {
          oos.close();
        } catch (Exception ioe) {
          // report nothing since an error has already been reported.
        }
      }
    }
    return file;
  }

  /**
   * Write object to temp file which is destroyed when the program exits.
   *
   * @param o object to be written to file
   * @param filename name of the temp file
   * @throws IOException If file cannot be written
   * @return File containing the object
   */
  public static File writeObjectToTempFile(Object o, String filename) throws IOException{
    File file = File.createTempFile(filename, ".tmp");
    file.deleteOnExit();
    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));
    oos.writeObject(o);
    oos.close();
    return file;
  }

  /**
   * Write object to a temp file and ignore exceptions.
   *
   * @param o object to be written to file
   * @param filename name of the temp file
   * @return File containing the object
   */
  public static File writeObjectToTempFileNoExceptions(Object o, String filename) {
    try {
      return writeObjectToTempFile(o, filename);
    } catch (Exception e) {
      System.err.println("Error writing object to file " + filename);
      e.printStackTrace();
      return null;
    }
  }


  /**
   * Read an object from a stored file.
   *
   * @param file the file pointing to the object to be retrived
   * @throws IOException If file cannot be read
   * @throws ClassNotFoundException If reading serialized object fails
   * @return the object read from the file.
   */
  public static <T> T readObjectFromFile(File file) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
    Object o = ois.readObject();
    ois.close();
    return ErasureUtils.<T>uncheckedCast(o);
  }

  /**
   * Read an object from a stored file.
   *
   * @param filename The filename of the object to be retrived
   * @throws IOException If file cannot be read
   * @throws ClassNotFoundException If reading serialized object fails
   * @return The object read from the file.
   */
  public static <T> T readObjectFromFile(String filename) throws IOException, ClassNotFoundException {
    return ErasureUtils.<T>uncheckedCast(readObjectFromFile(new File(filename)));
  }


  /**
   * Read an object from a stored file without throwing exceptions.
   *
   * @param file the file pointing to the object to be retrived
   * @return the object read from the file, or null if an exception occurred.
   */
  public static <T> T readObjectFromFileNoExceptions(File file) {
    Object o = null;
    try {
      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
      o = ois.readObject();
      ois.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return ErasureUtils.<T>uncheckedCast(o);
  }

  public static int lineCount(File textFile) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(textFile));
    int numLines = 0;
    while (r.readLine()!=null) {
      numLines++;
    }
    return numLines;
  }

  public static ObjectOutputStream writeStreamFromString(String serializePath) throws IOException {
    ObjectOutputStream oos;
    if (serializePath.endsWith(".gz")) {
      oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(serializePath))));
    } else {
      oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(serializePath)));
    }

    return oos;
  }

  public static ObjectInputStream readStreamFromString(String filenameOrUrl) throws IOException {
    ObjectInputStream in;
    InputStream is;
    if (filenameOrUrl.matches("https?://.*")) {
      URL u = new URL(filenameOrUrl);
      URLConnection uc = u.openConnection();
      is = uc.getInputStream();
    } else {
      is = new FileInputStream(filenameOrUrl);
    }
    if (filenameOrUrl.endsWith(".gz")) {
      in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(is)));
    } else {
      in = new ObjectInputStream(new BufferedInputStream(is));
    }
    return in;
  }

  private static InputStream getInputStreamFromString(String textFileOrUrl) throws IOException {
    InputStream is;
    if (textFileOrUrl.matches("https?://.*")) {
      URL u = new URL(textFileOrUrl);
      URLConnection uc = u.openConnection();
      is = uc.getInputStream();
    } else {
      is = new FileInputStream(textFileOrUrl);
    }
    if (textFileOrUrl.endsWith(".gz")) {
      is = new GZIPInputStream(is);
    }
    return is;
  }

  public static BufferedReader readReaderFromString(String textFileOrUrl) throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStreamFromString(textFileOrUrl)));
  }


  /** Open a BufferedReader to a file or URL specified by a String name.
   *  If the String starts with https?://, then it is interpreted as a URL,
   *  otherwise it is interpreted as a local file.  If the String ends in .gz,
   *  it is interpreted as a gzipped file (and uncompressed), else it is
   *  interpreted as a regular text file in the given encoding.
   *
   *  @param textFileOrUrl What to read from
   *  @param encoding CharSet encoding
   *  @return The BufferedReader
   *  @throws IOException If there is an I/O problem
   */
  public static BufferedReader readReaderFromString(String textFileOrUrl, String encoding) throws IOException {
    InputStream is = getInputStreamFromString(textFileOrUrl);
    return new BufferedReader(new InputStreamReader(is, encoding));
  }


  /**
   * Returns an Iterable of the lines in the file.
   *
   * The file reader will be closed when the iterator is exhausted.
   *
   * @param path  The file whose lines are to be read.
   * @return      An Iterable containing the lines from the file.
   * @throws IOException
   */
  public static Iterable<String> readLines(String path) {
    return IOUtils.readLines(new File(path));
  }

  /**
   * Returns an Iterable of the lines in the file.
   *
   * The file reader will be closed when the iterator is exhausted.
   *
   * @param file  The file whose lines are to be read.
   * @return      An Iterable containing the lines from the file.
   * @throws IOException
   */
  public static Iterable<String> readLines(final File file) {
    return readLines(file, null);
  }

  /**
   * Returns an Iterable of the lines in the file, wrapping the generated
   * FileInputStream with an instance of the supplied class.
   *
   * @param file  The file whose lines are to be read.
   * @param fileInputStreamWrapper  The class to wrap the InputStream with,
   *              e.g. GZIPInputStream. Note that the class must have a
   *              constructor that accepts an InputStream.
   * @return      An Iterable containing the lines from the file.
   */
  public static Iterable<String> readLines(final File file, final Class<? extends InputStream> fileInputStreamWrapper) {

    return new Iterable<String>() {
      public Iterator<String> iterator() {
        return new Iterator<String>() {

          protected BufferedReader reader = this.getReader();
          protected String line = this.getLine();

          public boolean hasNext() {
            return this.line != null;
          }

          public String next() {
            String line = this.line;
            this.line = this.getLine();
            return line;
          }

          protected String getLine() {
            try {
              String result = this.reader.readLine();
              if (result == null) {
                this.reader.close();
              }
              return result;
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          protected BufferedReader getReader() {
            try {
              InputStream stream = new FileInputStream(file);
              if (fileInputStreamWrapper != null) {
                stream = fileInputStreamWrapper.getConstructor(InputStream.class).newInstance(stream);
              }
              return new BufferedReader(new InputStreamReader(stream));
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  /**
   * Quietly close a stream.
   *
   * @param stream The stream to close
   * @throws RuntimeIOException If there is an IO problem. This is an unchecked exception
   */
  public static void close(InputStream stream) throws RuntimeIOException {
    try {
      stream.close();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  /**
   * Quietly opens a File.  If the file ends with a ".gz" extension,
   * automatically opens a GZIPInputStream to wrap the constructed
   * FileInputStream.
   */
  public static InputStream openFile(File file) throws RuntimeIOException {
    try {
      InputStream is = new BufferedInputStream(new FileInputStream(file));
      if (file.getName().endsWith(".gz")) {
        is = new GZIPInputStream(is);
      }
      return is;
    } catch (Exception e) {
      throw new RuntimeIOException(e);
    }
  }


}
