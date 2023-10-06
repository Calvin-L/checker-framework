package org.checkerframework.checker.calledmethods.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.InheritedAnnotation;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(EnsuresCalledMethodsOnException.List.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnsuresCalledMethodsOnException {

  /**
   * Returns Java expressions that have had the given methods called on them after the method throws
   * an exception.
   *
   * @return an array of Java expressions
   * @checker_framework.manual #java-expressions-as-arguments Syntax of Java expressions
   */
  String[] value();

  // NOTE 2023/10/6: There seems to be a fundamental limitation in the dataflow framework that
  // prevent this feature.  Specifically, every method has a SINGLE exceptional exit block, meaning
  // all information about what happens down different exception paths gets totally erased.
  //  /**
  //   * Returns the exception types under which the postcondition holds.
  //   *
  //   * @return the exception types under which the postcondition holds.
  //   */
  //  Class<? extends Throwable>[] exceptions();

  /**
   * The methods guaranteed to be invoked on the expressions if the result of the method throws an
   * exception.
   *
   * @return the methods guaranteed to be invoked on the expressions if the method throws an
   *     exception
   */
  String[] methods();

  /**
   * A wrapper annotation that makes the {@link EnsuresCalledMethodsOnException} annotation
   * repeatable. This annotation is an implementation detail: programmers generally do not need to
   * write this. It is created automatically by Java when a programmer writes more than one {@link
   * EnsuresCalledMethods} annotation at the same location.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
  @InheritedAnnotation
  public static @interface List {
    /**
     * Return the repeatable annotations.
     *
     * @return the repeatable annotations
     */
    EnsuresCalledMethodsOnException[] value();
  }
}
