package io.rubduk

import zio.Has

package object domain {

  type ItemRepository = Has[ItemRepository.Service]
}
