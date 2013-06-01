Domgen.repository(:FGIS) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:jaxrs)
  repository.enable_facet(:xml)

  repository.jpa.provider = :eclipselink
  repository.jpa.exclude_unlisted_classes = false
  repository.jpa.properties["eclipselink.session-event-listener"] = "fgis.server.support.jpa.ConverterInitializer"

  repository.data_module(:FGIS) do |data_module|

    data_module.entity(:Resource) do |t|
      t.integer(:ID, :primary_key => true)
      t.text(:Type, :immutable => true)
      t.text(:Name, :immutable => true)
      t.query('findByName')
      t.query('findAllByTypeInArea') do |q|
        q.text(:Type, :collection_type => :sequence)
        q.jpa.jpql = "O.Type IN :Type"
      end
      t.query('findAllInArea')
    end

    data_module.entity(:ResourceTrack) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Resource, :immutable => true, :"inverse.traversable" => true)
      t.datetime(:CollectedAt, :immutable => true)
      t.point(:Location, :immutable => true)

      t.query('findAllByResourceSince') do |q|
        q.jpa.jpql = "O.Resource.ID = :ResourceID AND O.CollectedAt >= :StartAt"
        q.integer(:ResourceID)
        q.datetime(:StartAt)
      end

      t.sql.index([:Location], :index_type => :gist)
    end
  end
end
