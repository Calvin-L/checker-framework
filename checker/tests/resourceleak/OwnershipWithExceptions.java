// Test case for https://github.com/typetools/checker-framework/issues/6179

import java.io.Closeable;
import java.io.IOException;
import org.checkerframework.checker.calledmethods.qual.*;
import org.checkerframework.checker.mustcall.qual.CreatesMustCallFor;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.dataflow.qual.Pure;

abstract class OwnershipWithExceptions {

  abstract @Owning Closeable alloc();

  @Pure
  abstract boolean arbitraryChoice();

  abstract void transfer(@Owning Closeable resource) throws IOException;

  void transferAndPropagateException(@Owning Closeable resource) throws IOException {
    transfer(resource);
  }

  void transferHasNoObligationsOnException(@Owning Closeable resource) throws IOException {
    throw new IOException();
  }

  // :: error: (required.method.not.called)
  void transferAndIgnoreExceptionWithoutClosing(@Owning Closeable zzz) {
    try {
      transfer(zzz);
    } catch (IOException ignored) {
    }
  }

  boolean transferAndIgnoreExceptionCorrectly(@Owning Closeable resource) {
    try {
      transfer(resource);
      return true;
    } catch (Exception e) {
      try {
        resource.close();
      } catch (Exception other) {
      }
      return false;
    }
  }

  // Passing an argument as an @Owning parameter does not transfer ownership if
  // the called method throws.  So, this is not correct: if transfer(resource)
  // throws an exception, it leaks the resource.
  void noExceptionHandling() throws IOException {
    // ::error: (required.method.not.called)
    Closeable resource = alloc();
    // ::error: (assignment)
    @CalledMethods("close") Closeable a = resource;
    transfer(resource);
    // ::error: (assignment)
    @CalledMethods("close") Closeable b = resource;
  }

  class OwnedField implements Closeable {

    final @Owning Closeable resource;

    OwnedField() throws IOException {
      // Field assignments in constructors are special.  When the constructor
      // exits by exception, the field becomes permanently inaccessible, and
      // therefore the allocated resource is leaked.
      // :: error: (required.method.not.called)
      resource = alloc();
      if (arbitraryChoice()) {
        throw new IOException();
      }
    }

    OwnedField(int ignored) throws IOException {
      // Same as the 0-argument constructor, but handled correctly (algorithm 1).
      resource = alloc();
      try {
        if (arbitraryChoice()) {
          throw new IOException();
        }
      } catch (Exception e) {
        resource.close();
        throw e;
      }
    }

    OwnedField(float ignored) throws IOException {
      // Same as the 0-argument constructor, but handled correctly (algorithm 2).
      Closeable r = alloc();
      resource = r;
      try {
        if (arbitraryChoice()) {
          throw new IOException();
        }
      } catch (Exception e) {
        r.close();
        throw e;
      }
    }

    OwnedField(@Owning Closeable resource, int arg) throws IOException {
      // On exception, ownership of the @Owning argument remains with the caller.
      // So, this constructor is OK.
      if (arbitraryChoice()) {
        throw new IOException();
      }
      this.resource = resource;
    }

    OwnedField(@Owning Closeable resource, boolean arg) throws IOException {
      // Same as the previous constructor, but in the other order.
      this.resource = resource;
      if (arbitraryChoice()) {
        throw new IOException();
      }
    }

    @Override
    @EnsuresCalledMethods(
        value = "this.resource",
        methods = {"close"})
    public void close() throws IOException {
      resource.close();
    }
  }

  class MutableOwnedField implements Closeable {

    @Owning Closeable resource;

    @RequiresCalledMethods(
        value = "this.resource",
        methods = {"close"})
    @CreatesMustCallFor("this")
    void realloc() throws IOException {
      // Unlike in a constructor, field assignments in normal methods are not
      // leaked when the method exits with an exception, since the reciever
      // is still accessible to the caller.
      resource = alloc();
      if (arbitraryChoice()) {
        throw new IOException();
      }
    }

    @Override
    @EnsuresCalledMethods(
        value = "this.resource",
        methods = {"close"})
    public void close() throws IOException {
      resource.close();
    }
  }
}
