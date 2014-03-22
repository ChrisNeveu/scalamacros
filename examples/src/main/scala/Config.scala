package prog.config

import prog.mac.config.{ ConfigLoader, Config }
import org.yaml.snakeyaml.Yaml

object Bar {
	val config = ConfigLoader.load("config.yaml")
}

