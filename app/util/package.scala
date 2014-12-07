/**
 * Created by bj on 06.12.14.
 */
package models

import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}

package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
    }
    context
  }
}