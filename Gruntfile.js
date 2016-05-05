module.exports = function(grunt) {

    grunt.initConfig({
        pkg         : grunt.file.readJSON('package.json'),
        concat      : {
            options      : {
                separator: ''
            },
            databaseconnector  : {
                src : ['src/main/javascript/components/main/databaseConnectorModule.js', 'src/main/javascript/components/main/connections/**/*.js', 'src/main/javascript/components/main/connectionManagement/**/*.js'],
                dest: 'src/main/resources/javascript/angular/components/dc-main.js'
            },
            core         : {
                src : ['src/main/javascript/components/*.js'],
                dest: 'src/main/resources/javascript/angular/components/dc-core.js'
            }
        },
        copy     : {
            main: {
                files: [
                    {
                        expand: true,
                        cwd   : 'src/main/javascript/',
                        src   : ['components/**/*.html', 'components/**/*.json'],
                        dest  : 'src/main/resources/javascript/angular'
                    }]
            }
        },
        watch       : {
            files: ['Gruntfile.js', 'src/main/javascript/**/*.js', 'src/main/javascript/**/*.html'],
            tasks: ['concat', 'copy', 'less', 'uglify']
        },
        bower_concat: {
            all: {
                dest        : 'src/main/resources/javascript/lib/_dc.js',
                cssDest     : 'src/main/resources/css/lib/_dc.css',
                exclude     : [
                    'jquery',
                    'modernizr',
                    'bootstrap',
                    'bootswatch-dist',
                    'datatables'
                ],
                mainFiles   : {
                    'datatables-plugins': ['sorting/datetime-moment.js'],
                    'moment'            : ['min/moment-with-locales.js']
                },
                dependencies: {
                    'datatables-plugins': ['datatables.net', 'moment'],
                    'datatables.net-buttons': ['datatables.net', 'pdfmake', 'jszip'],
                    'angular-datatables': ['datatables.net'],
                    'underscore': ['pdfmake']
                },
                bowerOptions: {
                    relative: false
                }
            }
        },
        uglify: {
            options: {
                mangle: false,
                compress: false,
                beautify:true
            },
            my_target: {
                files: {
                    'src/main/resources/javascript/lib/_dc.min.js': ['src/main/resources/javascript/lib/_dc.js'],
                    'src/main/resources/javascript/lib/_dc-main.min.js': ['src/main/resources/javascript/angular/components/dc-main.js','src/main/resources/javascript/angular/components/dc-core.js'],
                    
                }
            }
        },
        less: {
            development: {
                options: {
                    compress: true  //minifying the result
                },
                files: {
                    "./src/main/resources/css/bootstrap.min.css":"./src/main/less/bootstrap.less"
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-bower-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.registerTask('default', ['concat', 'bower_concat', 'copy', 'less', 'uglify']);

};
