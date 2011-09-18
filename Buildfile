#                                                                  -*- ruby -*-
# Buildr Buildfile for curn
#
# To build curn, you'll need Apache Buildr.
# ---------------------------------------------------------------------------

# Environment variables

ENV['USER'] ||= 'bmc'
ENV['HOME'] ||= "/home/#{ENV['USER']}"
ENV['IZPACK_HOME'] ||= "#{ENV['HOME']}/java/IzPack"

# Constants

CURN_VERSION          = '3.2.8'
GROUP                 = 'org.clapper'
CURN_JAR_NAME         = 'curn'
CURN_BOOT_JAR_NAME    = 'curn-boot'
CURN_PLUGINS_JAR_NAME = 'curn-plugins'

# Dependencies.
ASM_VERSION      = '3.3.1'

JAVAUTIL         = 'org.clapper:javautil:jar:3.0.1'
FREEMARKER       = 'org.freemarker:freemarker:jar:2.3.14'
JAVAMAIL         = 'javax.mail:mail:jar:1.4.4'
JDOM             = 'org.jdom:jdom:jar:1.1'
COMMONS_LOGGING  = 'commons-logging:commons-logging:jar:1.1.1'
ROME             = 'rome:rome:jar:1.0'
ASM              = "asm:asm:jar:#{ASM_VERSION}"
ASM_COMMONS      = "asm:asm-commons:jar:#{ASM_VERSION}"
COMPILE_ARTIFACTS= [ASM, ASM_COMMONS, COMMONS_LOGGING, FREEMARKER, JAVAMAIL,
                    JAVAUTIL, JDOM, ROME]

IZPACK_VERSION   = '4.3.4'
IZPACK = "org.codehaus.izpack:izpack-standalone-compiler:jar:#{IZPACK_VERSION}"

# Some local tasks and task aliases
Project.local_task :install

define 'curn' do
  project.version = CURN_VERSION
  project.group   = GROUP

  # Repos
  repositories.remote << 'http://download.java.net/maven/2'
  repositories.remote << 'http://www.ibiblio.org/maven2/'
  repositories.remote << 'http://maven.clapper.org/'

  # Compilation
  compile.using :target => '1.6', :lint => 'all', :deprecation => true
  compile.with ASM, ASM_COMMONS, COMMONS_LOGGING, FREEMARKER, JAVAMAIL,
               JAVAUTIL, JDOM, ROME
  # Main jar
  package(:jar, :id => CURN_JAR_NAME).
    exclude(_('target/classes/**/Bootstrap*.class')).
    exclude(_('target/classes/**/plugins/*.class'))

  # Plugins jar
  package(:jar, :id => CURN_PLUGINS_JAR_NAME).
    clean.
    include('target/classes/org/clapper/curn/plugins',
            :as => 'org/clapper/curn/plugins')

  # Bootstrap jar
  package(:jar, :id => CURN_BOOT_JAR_NAME).
    clean.
    include('target/classes/org/clapper/curn/Bootstrap.class',
            :as => 'org/clapper/curn/Bootstrap.class').
    include('target/classes/org/clapper/curn/BootstrapException.class',
            :as => 'org/clapper/curn/BootstrapException.class')

  PROPERTY_FILE_VARIABLES = {
    'version'   => project.version,
    'name'      => name,
    'copyright' => 'Copyright (c) 2004-2011 Brian M. Clapper.'
  }

  resources.filter.using :maven, PROPERTY_FILE_VARIABLES

  # ----------------------------------------------------------------------
  # The IzPack installer
  # ----------------------------------------------------------------------

  # Create the installer jar.
  task :installer => [:package, :doc, :installerxml] do
    # Verify installation of IzPack.
    IZPACK_HOME = ENV['IZPACK_HOME']
    puts IZPACK_HOME
    if !File.exists? IZPACK_HOME
      raise Exception.new "IZPACK_HOME #{IZPACK_HOME} doesn't exist."
    elsif !File.directory? IZPACK_HOME
      raise Exception.new "IZPACK_HOME #{IZPACK_HOME} is not a directory."
    end

    # Create the installation temporary directory.
    mkdir_p TMP_DIR

    begin
      # Copy the dependent jars there.
      compile.dependencies.each do |d|
        cp d.to_s, TMP_DIR
      end

      # Generate the installer. This fails, for some reason:
      #
      # sh "#{IZPACK_HOME}/bin/compile", _('target/install.xml'),
      #    '-b', TOP_DIR, '-h', IZPACK_HOME,
      #    '-o ', _("target/curn-installer-#{version}.jar")
      #
      # So, we're bailing, and falling back to Ant.
      ant("installer") do |proj|
        # Define the Ant task.
        proj.taskdef :name => 'izpack',
        :classname => 'com.izforge.izpack.ant.IzPackTask',
        :classpath => Dir.glob("#{IZPACK_HOME}/lib/*.jar").join(":")

        # Invoke it.
        proj.izpack :input => _('target/install.xml'),
        :output => _("target/curn-installer-#{version}.jar"),
        :installerType => 'standard',
        :basedir => '.',
        :izPackDir => IZPACK_HOME
      end

    else
      # Remove the temporary directory.
      rm_r TMP_DIR
    end
  end

  # Create the installer XML from the XML template. NOTE: IzPack must
  # already be installed, and its home directory must be specified via the
  # IZPACK_HOME environment variable.
  task :installerxml do
    # Filter the template install.xml file into one with the variables
    # substituted
    TOP_DIR = _(".")
    TMP_DIR = _('target/install-tmp')
    filter('src/installer').
      into('target').
      include('*.xml').
      using('CURN_VERSION'      => version,
            'INSTALL_TMP'       => TMP_DIR,
            'TOP_DIR'           => TOP_DIR,
            'SRC_INSTALL'       => "#{TOP_DIR}/src/installer",
            'RELEASE_DIR'       => "#{TOP_DIR}/target",
            'CURN_JAR_FILE'     => "#{CURN_JAR_NAME}-#{version}.jar",
            'CURNBOOT_JAR_FILE' => "#{CURN_BOOT_JAR_NAME}-#{version}.jar",
            'PLUGINS_JAR_FILE'  => "#{CURN_PLUGINS_JAR_NAME}-#{version}.jar",
            'ASM_VERSION'       => ASM_VERSION,
            'DOCS_DIR'          => _('docs'),
            'DEP_JAR_DIR'       => TMP_DIR,
            'JAVADOCS_DIR'      => _('target/doc')).
      run
  end

end


