({
    newSession: function() {
        return {
            sql: '',
            '&types.@typeName = {fields: #[{name: @fieldName, type: {name: @fieldTypeName}} @*fields]}': function(typeName, fields) {
                this.sql += 'CREATE TABLE ' + typeName + '(' +
                    fields.stream().map(function(f) {return f.fieldName + ' ' + f.fieldTypeName}).collect(Java.type('java.util.stream.Collectors').joining(', ')) +
                    ')\n'
            },
            '&types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}}': function(typeName, fieldName, fieldTypeName) {
                this.sql += 'ALTER TABLE ' + fieldName + ' ADD COLUMN ' + fieldName + ' ' + fieldTypeName + "\n"
            },
            close: function() {
                print(this.sql)
            }
        }
    }
})