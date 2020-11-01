package io.rubduk

import io.rubduk.api.routes.Api
import zio.Has

package object api {
  type Api = Has[Api.Service]
}
