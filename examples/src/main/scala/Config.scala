package prog.config

import prog.mac.config.LoadedConf
import org.yaml.snakeyaml.Yaml

object Qux {
	@LoadedConf val foo = 6
}
