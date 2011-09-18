#                                                                  -*- ruby -*-
# Buildr Buildfile for curn
# ---------------------------------------------------------------------------

# Dependencies.
JAVAUTIL         = 'org.clapper:javautil:jar:3.0.1'
FREEMARKER       = 'org.freemarker:freemarker:jar:2.3.14'
JAVAMAIL         = 'javax.mail:mail:jar:1.4.4'
JDOM             = 'org.jdom:jdom:jar:1.1'
COMMONS_LOGGING  = 'commons-logging:commons-logging:jar:1.1.1'
ROME             = 'rome:rome:jar:1.0'
ASM              = 'asm:asm:jar:3.3.1'
ASM_COMMONS      = 'asm:asm-commons:jar:3.3.1'

# Some local tasks and task aliases
Project.local_task :install

define 'curn' do
  project.version = '3.2.8'
  project.group   = 'org.clapper'

  package :jar

  repositories.remote << 'http://download.java.net/maven/2'
  repositories.remote << 'http://www.ibiblio.org/maven2/'
  repositories.remote << 'http://maven.clapper.org/'

  compile.using :target => '1.6', :lint => 'all', :deprecation => true
  compile.with ASM, ASM_COMMONS, COMMONS_LOGGING, FREEMARKER, JAVAMAIL,
               JAVAUTIL, JDOM, ROME

  task :install => :package do
    puts "Not done yet"
  end
end


