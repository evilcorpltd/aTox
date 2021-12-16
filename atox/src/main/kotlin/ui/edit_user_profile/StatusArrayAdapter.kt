package ltd.evilcorp.atox.ui.edit_user_profile

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import ltd.evilcorp.atox.R

class StatusArrayAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    private val strings: Array<String>,
) : ArrayAdapter<String>(context, resource, textViewResourceId, strings) {

    private val statusFilter = StatusFilter()  // This filter doesn't filter :)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val imageView: ImageView = view.findViewById(R.id.ic_status_indicator)

        when (getItem(position)) {
            context.getString(R.string.status_available) -> imageView.setImageResource(R.drawable.ic_available)
            context.getString(R.string.status_away) -> imageView.setImageResource(R.drawable.ic_away)
            context.getString(R.string.status_busy) -> imageView.setImageResource(R.drawable.ic_busy)
        }

        return view
    }

    override fun getFilter(): Filter {
        return statusFilter
    }

    inner class StatusFilter : Filter() {
        override fun performFiltering(prefix: CharSequence): FilterResults {
            val results = FilterResults()
            results.values = strings;
            results.count = strings.size;

            return results;
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) = Unit
    }

}
