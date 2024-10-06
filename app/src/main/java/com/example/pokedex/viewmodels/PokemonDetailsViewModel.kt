package com.example.pokedex.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.models.AbilityDescription
import com.example.pokedex.adapters.models.AdapterItemAbility
import com.example.pokedex.adapters.models.AdapterItemEvolutionChainEdge
import com.example.pokedex.models.EvolutionChainEntry
import com.example.pokedex.models.Description
import com.example.pokedex.models.Pokemon
import com.example.pokedex.models.PokemonDetails
import com.example.pokedex.models.State
import com.example.pokedex.adapters.models.AdapterItemType
import com.example.pokedex.repositories.Repository
import com.example.pokedex.repositories.Result
import com.example.pokedex.utils.FRACTION_FOUR
import com.example.pokedex.utils.FRACTION_ONE_HALF
import com.example.pokedex.utils.FRACTION_ONE_QUATER
import com.example.pokedex.utils.FRACTION_TWO
import com.example.pokedex.utils.FRACTION_ZERO
import com.example.pokedex.utils.unsqueeze
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailsViewModel @Inject constructor(
    val repository: Repository
): ViewModel() {
    companion object {
        private val weeknessPlaceholders = (0 until 4).map(AdapterItemType::Placeholder)
        private val abilityPlaceholders = (0 until 1).map(AdapterItemAbility::PlaceHolder)
        private val evolutionPlaceholders = (0 until 2).map(AdapterItemEvolutionChainEdge::Placeholder)

        private const val NESTED_SCROLL_VIEW = 0
        private const val ERROR_MESSAGE = 1
    }

    private val _result = MutableStateFlow<PokemonDetails?>(null)
    @OptIn(FlowPreview::class)
    val result = _result.asStateFlow()

    private val _state = MutableStateFlow(State.LOADING)
    val state = _state.asStateFlow()

    val displayedChild = state.map { state ->
        when (state) {
            State.SUCCESS -> NESTED_SCROLL_VIEW
            State.LOADING -> NESTED_SCROLL_VIEW
            State.ERROR -> ERROR_MESSAGE
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NESTED_SCROLL_VIEW)

    val favourites = repository.getFavouritePokemonFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favouriteIdSet = favourites.map { favourites -> favourites.map(Pokemon::id).toSet() }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun setIsFavourite(pokemon: Pokemon, isFavourite: Boolean) {
        var favourites = favourites.value.asSequence().filter { it.id != pokemon.id }
        if (isFavourite) favourites += pokemon
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavourites(favourites.toList())
        }
    }

    private val _weeknessQuater = MutableStateFlow<List<AdapterItemType>>(weeknessPlaceholders)
    val weeknessQuater = _weeknessQuater.asStateFlow()
    private val _weeknessHalf = MutableStateFlow<List<AdapterItemType>>(weeknessPlaceholders)
    val weeknessHalf = _weeknessHalf.asStateFlow()
    private val _weeknessDouble = MutableStateFlow<List<AdapterItemType>>(weeknessPlaceholders)
    val weeknessDouble = _weeknessDouble.asStateFlow()
    private val _weeknessQuadruple = MutableStateFlow<List<AdapterItemType>>(weeknessPlaceholders)
    val weeknessQuadruple = _weeknessQuadruple.asStateFlow()
    private val _weeknessImmune = MutableStateFlow<List<AdapterItemType>>(weeknessPlaceholders)
    val weeknessImmune = _weeknessImmune.asStateFlow()
    private val _descriptions = MutableStateFlow(listOf<Description>())
    val descritions = _descriptions.asStateFlow()
    private val _abilityDescriptions = MutableStateFlow(listOf<AbilityDescription>())
    val abilityDescriptions = _abilityDescriptions.asStateFlow()
    private val _evolutions = MutableStateFlow<List<AdapterItemEvolutionChainEdge>>(evolutionPlaceholders)
    val evolutions = _evolutions.asStateFlow()

    private val _abilities = MutableStateFlow<List<AdapterItemAbility>>(abilityPlaceholders)
    val abilities = _abilities.asStateFlow()

    private fun toTypeItems(multiplier: AdapterItemType.Multiplier, typeIds: List<Int>): List<AdapterItemType> {
        if (typeIds.isEmpty()) {
            return emptyList()
        }
        return multiplier.unsqueeze() + typeIds.map(AdapterItemType::Type)
    }

    fun chainToEdges(entries: List<Pokemon>): List<AdapterItemEvolutionChainEdge> {
        val adjacency = entries.groupBy { entry -> entry.specyEvolvedFromSpecyId  }

        val rootNodes = entries.filter { entry -> entry.specyEvolvedFromSpecyId == null }
        val queue = ArrayDeque(rootNodes)
        val result = mutableListOf<AdapterItemEvolutionChainEdge>()
        val visited = mutableSetOf<Int>()

        var id = 0
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val entryAlreadyVisited = visited.add(current.specyId)
            if (!entryAlreadyVisited) {
                continue
            }
            adjacency[current.specyId]?.forEach { target ->
                result.add(AdapterItemEvolutionChainEdge.EvolutionChainEdge(EvolutionChainEntry(id, current), EvolutionChainEntry(id + 1, target)))
                queue.add(target)
                id += 2
            }
        }

        return result
    }

    private val pokemonId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            pokemonId.filterNotNull().collect { pokemonId ->
                loadHelper(pokemonId)
            }
        }
    }

    fun reload() {
        val pokemonId = pokemonId.value
        if (pokemonId == null) {
            return
        }
        loadHelper(pokemonId)
    }

    fun load(pokemonId: Int) {
        this.pokemonId.value = pokemonId
    }

    private fun loadHelper(pokemonId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = State.LOADING
            _weeknessQuater.value = weeknessPlaceholders
            _weeknessHalf.value = weeknessPlaceholders
            _weeknessDouble.value = weeknessPlaceholders
            _weeknessQuadruple.value = weeknessPlaceholders
            _evolutions.value = evolutionPlaceholders

            when(val result = repository.getPokemonDetails(pokemonId)) {
                is Result.Error -> {
                    _state.value = State.ERROR
                }
                is Result.Success -> {
                    _state.value = State.SUCCESS
                    _result.value = result.data
                    _abilities.value = result.data.abilities.map(AdapterItemAbility::Ability)
                    _weeknessQuater.value = result.data.typeWeekness
                        .getOrDefault(FRACTION_ONE_QUATER, emptyList())
                        .let{toTypeItems(AdapterItemType.Quater, it)}
                    _weeknessHalf.value = result.data.typeWeekness
                        .getOrDefault(FRACTION_ONE_HALF, emptyList())
                        .let{toTypeItems(AdapterItemType.Half, it)}
                    _weeknessDouble.value = result.data.typeWeekness
                        .getOrDefault(FRACTION_TWO, emptyList())
                        .let{toTypeItems(AdapterItemType.Double, it)}
                    _weeknessQuadruple.value = result.data.typeWeekness
                        .getOrDefault(FRACTION_FOUR, emptyList())
                        .let{toTypeItems(AdapterItemType.Quadruple, it)}
                    _weeknessImmune.value = result.data.typeWeekness
                        .getOrDefault(FRACTION_ZERO, emptyList())
                        .let{toTypeItems(AdapterItemType.Immune, it)}
                    _descriptions.value = result.data.specyDescriptions
                    _evolutions.value = result.data.specyEvolutionChain.let(::chainToEdges)
                }
            }
        }
    }
}